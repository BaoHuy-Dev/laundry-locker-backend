package com.huynqb.laundrylockerbackend.module.auth.service;

import static com.huynqb.laundrylockerbackend.core.constant.MessageConstants.*;

import com.google.firebase.auth.FirebaseToken;
import com.huynqb.laundrylockerbackend.core.firebase.FirebaseService;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.CompleteRegistrationRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.EmailCompleteRegistrationRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.EmailSendOtpRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.EmailVerifyOtpRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.LogoutRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.PhoneLoginRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.RefreshTokenRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.EmailLoginResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.PhoneLoginResponse;
import com.huynqb.laundrylockerbackend.module.auth.exception.AuthenticationException;
import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService - Handles phone and email OTP authentication. Uses Redis via TokenService for
 * high-performance token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final TokenService tokenService;
  private final JwtTokenProvider jwtTokenProvider;
  private final FirebaseService firebaseService;
  private final EmailOtpService emailOtpService;
  private final com.huynqb.laundrylockerbackend.module.auth.mapper.AuthMapper authMapper;

  @Value("${app.security.jwt.expiration-ms}")
  private long jwtExpirationMs;

  @Value("${app.security.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  // ===== Phone Authentication =====

  /**
   * Authenticate user with Firebase phone token. Returns isNewUser flag if user needs to complete
   * registration.
   *
   * @param request Phone login request with Firebase ID token
   * @return PhoneLoginResponse with tokens and isNewUser flag
   */
  @Transactional
  public PhoneLoginResponse phoneLogin(PhoneLoginRequest request) {

    // Verify Firebase ID token
    FirebaseToken firebaseToken = firebaseService.verifyIdToken(request.getIdToken());

    // Extract phone number from token
    String phoneNumber = firebaseService.extractPhoneNumber(firebaseToken);

    // Check if user exists
    var existingUser = userRepository.findByPhoneNumber(phoneNumber);

    if (existingUser.isPresent()) {
      // Existing user - generate tokens and return
      User user = existingUser.get();
      user.setPhoneVerified(true);
      userRepository.save(user);

      String accessToken = jwtTokenProvider.generateTokenFromUser(user);
      String refreshToken = createRefreshToken(user);

      log.info("Phone login successful for existing user: {}", phoneNumber);

      return authMapper.toPhoneLoginResponse(accessToken, refreshToken, jwtExpirationMs / 1000);
    } else {
      // New user - generate temp token and save to Redis for registration
      String tempToken = UUID.randomUUID().toString();
      long tempTokenTtl = 600000L; // 10 minutes
      tokenService.saveTempRegistrationToken(tempToken, phoneNumber, tempTokenTtl);

      log.info("New phone user detected, needs registration: {}", phoneNumber);

      return authMapper.toNewUserPhoneResponse(phoneNumber, tempToken);
    }
  }

  /**
   * Complete registration for new phone users. Called after phone OTP verification. Supports both
   * tempToken (preferred) and Firebase idToken authentication.
   *
   * @param request Registration details with tempToken or Firebase ID token
   * @return Authentication response with JWT tokens
   */
  @Transactional
  public AuthResponse completeRegistration(CompleteRegistrationRequest request) {
    String phoneNumber;
    String providerId;

    // Try tempToken first (preferred method)
    if (request.getTempToken() != null && !request.getTempToken().isBlank()) {
      phoneNumber = tokenService.getIdentifierByTempToken(request.getTempToken());
      if (phoneNumber == null) {
        throw new AuthenticationException(E_AUTH008); // Invalid or expired temp token
      }
      // Delete temp token after use
      tokenService.deleteTempToken(request.getTempToken());
      // Generate providerId from phone number for tempToken flow
      providerId = "phone_" + phoneNumber.replaceAll("[^0-9]", "");
    } else if (request.getIdToken() != null && !request.getIdToken().isBlank()) {
      // Fallback to Firebase ID token
      FirebaseToken firebaseToken = firebaseService.verifyIdToken(request.getIdToken());
      phoneNumber = firebaseService.extractPhoneNumber(firebaseToken);
      providerId = firebaseToken.getUid();
    } else {
      throw new AuthenticationException(E_AUTH009); // Either tempToken or idToken required
    }

    // Check if user already exists
    if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
      throw new AuthenticationException(E_AUTH005);
    }

    // Create new user with profile info
    User newUser =
        User.builder()
            .phoneNumber(phoneNumber)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .name(request.getFirstName() + " " + request.getLastName())
            .birthday(request.getBirthday())
            .provider(AuthProvider.PHONE)
            .providerId(providerId)
            .phoneVerified(true)
            .emailVerified(false)
            .roles(getDefaultRoles())
            .build();

    User savedUser = userRepository.save(newUser);
    log.info("New user registered via phone: {}", phoneNumber);

    // Generate JWT tokens
    String accessToken = jwtTokenProvider.generateTokenFromUser(savedUser);
    String refreshToken = createRefreshToken(savedUser);

    return authMapper.toAuthResponse(accessToken, refreshToken, jwtExpirationMs / 1000);
  }

  // ===== Email OTP Authentication =====

  /**
   * Send OTP to email for authentication.
   *
   * @param request Email send OTP request
   * @return true if OTP sent successfully
   */
  public boolean sendEmailOtp(EmailSendOtpRequest request) {
    String email = request.getEmail().trim().toLowerCase();
    log.info("Sending OTP to email: {}", email);
    return emailOtpService.sendOtp(email);
  }

  /**
   * Verify email OTP and check if user exists. Returns tokens for existing users, or isNewUser flag
   * for new users.
   *
   * @param request Email verify OTP request
   * @return EmailLoginResponse with tokens or isNewUser flag
   */
  @Transactional
  public EmailLoginResponse verifyEmailOtp(EmailVerifyOtpRequest request) {
    String email = request.getEmail().trim().toLowerCase();

    // Verify OTP
    if (!emailOtpService.verifyOtp(email, request.getOtp())) {
      throw new AuthenticationException(E_OTP001);
    }

    // Check if user exists
    var existingUser = userRepository.findByEmail(email);

    if (existingUser.isPresent()) {
      // Existing user - generate tokens
      User user = existingUser.get();
      user.setEmailVerified(true);
      userRepository.save(user);

      String accessToken = jwtTokenProvider.generateTokenFromUser(user);
      String refreshToken = createRefreshToken(user);

      log.info("Email login successful for existing user: {}", email);

      return authMapper.toEmailLoginResponse(accessToken, refreshToken, jwtExpirationMs / 1000);
    } else {
      // New user - return flag to complete registration
      // Generate temp token and save to Redis for registration
      String tempToken = UUID.randomUUID().toString();
      long tempTokenTtl = 600000L; // 10 minutes
      tokenService.saveTempRegistrationToken(tempToken, email, tempTokenTtl);

      log.info("New email user detected, needs registration: {}", email);

      return authMapper.toNewUserEmailResponse(tempToken);
    }
  }

  /**
   * Complete registration for new email users. Called after email OTP verification.
   *
   * @param request Registration details with email and OTP
   * @return Authentication response with JWT tokens
   */
  @Transactional
  public AuthResponse emailCompleteRegistration(EmailCompleteRegistrationRequest request) {
    // Validate temp token
    String email = tokenService.getIdentifierByTempToken(request.getTempToken());

    if (email == null) {
      throw new AuthenticationException(E_AUTH008); // Invalid or expired temp token
    }

    // Delete temp token after use
    tokenService.deleteTempToken(request.getTempToken());

    // Check if user already exists
    if (userRepository.findByEmail(email).isPresent()) {
      throw new AuthenticationException(E_AUTH005);
    }

    // Create new user with profile info
    User newUser =
        User.builder()
            .email(email)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .name(request.getFirstName() + " " + request.getLastName())
            .birthday(request.getBirthday())
            .provider(AuthProvider.EMAIL)
            .emailVerified(true)
            .phoneVerified(false)
            .roles(getDefaultRoles())
            .build();

    User savedUser = userRepository.save(newUser);
    log.info("New user registered via email: {}", email);

    // Generate JWT tokens
    String accessToken = jwtTokenProvider.generateTokenFromUser(savedUser);
    String refreshToken = createRefreshToken(savedUser);

    return authMapper.toAuthResponse(accessToken, refreshToken, jwtExpirationMs / 1000);
  }

  // ===== Token Management =====

  /**
   * Refresh access token using refresh token
   *
   * @param request Refresh token request
   * @return New authentication response
   */
  @Transactional
  public AuthResponse refreshToken(RefreshTokenRequest request) {
    String refreshTokenValue = request.getRefreshToken();

    // Check if refresh token exists in Redis
    Long userId = tokenService.getUserIdByRefreshToken(refreshTokenValue);
    if (userId == null) {
      throw new AuthenticationException("E_AUTH004");
    }

    // Find user
    User user =
        userRepository.findById(userId).orElseThrow(() -> new AuthenticationException("E_AUTH004"));

    // Generate new access token
    String newAccessToken = jwtTokenProvider.generateTokenFromUser(user);

    log.info("Access token refreshed for user ID: {}", user.getId());

    return authMapper.toAuthResponse(newAccessToken, refreshTokenValue, jwtExpirationMs / 1000);
  }

  /**
   * Logout user by blacklisting access token and deleting refresh token.
   *
   * @param accessToken Current access token
   * @param request Logout request with refresh token
   */
  @Transactional
  public void logout(String accessToken, LogoutRequest request) {
    // Blacklist access token in Redis
    if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
      String identifier = jwtTokenProvider.getEmailFromToken(accessToken);

      // Calculate remaining TTL for the token
      long remainingMs = jwtTokenProvider.getRemainingExpirationMs(accessToken);
      if (remainingMs > 0) {
        tokenService.blacklistAccessToken(accessToken, remainingMs);
        log.info("Access token blacklisted in Redis for user: {}", identifier);
      }
    }

    // Delete refresh token from Redis
    String refreshTokenValue = request.getRefreshToken();
    if (refreshTokenValue != null) {
      tokenService.deleteRefreshToken(refreshTokenValue);
      log.info("Refresh token deleted from Redis");
    }
  }

  /**
   * Check if access token is blacklisted
   *
   * @param token Access token
   * @return true if blacklisted
   */
  public boolean isTokenBlacklisted(String token) {
    return tokenService.isAccessTokenBlacklisted(token);
  }

  // ===== Private Helper Methods =====

  /**
   * Create refresh token and save to Redis
   *
   * @param user User entity
   * @return Refresh token string
   */
  private String createRefreshToken(User user) {
    String tokenValue = UUID.randomUUID().toString();

    // Save to Redis with TTL
    tokenService.saveRefreshToken(tokenValue, user.getId(), refreshExpirationMs);

    log.debug("Refresh token saved to Redis for user ID: {}", user.getId());
    return tokenValue;
  }

  /**
   * Get default roles for new users
   *
   * @return Set of default roles
   */
  private Set<Role> getDefaultRoles() {
    Set<Role> roles = new HashSet<>();
    roleRepository.findByName(RoleName.USER).ifPresent(roles::add);
    return roles;
  }
}
