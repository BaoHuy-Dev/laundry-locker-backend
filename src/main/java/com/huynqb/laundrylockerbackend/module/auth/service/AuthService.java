package com.huynqb.laundrylockerbackend.module.auth.service;

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

      return PhoneLoginResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .expiresIn(jwtExpirationMs / 1000)
          .isNewUser(false)
          .build();
    } else {
      // New user - return flag to complete registration
      log.info("New phone user detected, needs registration: {}", phoneNumber);

      return PhoneLoginResponse.builder().isNewUser(true).build();
    }
  }

  /**
   * Complete registration for new phone users. Called after phone OTP verification.
   *
   * @param request Registration details with Firebase ID token
   * @return Authentication response with JWT tokens
   */
  @Transactional
  public AuthResponse completeRegistration(CompleteRegistrationRequest request) {

    // Verify Firebase ID token again
    FirebaseToken firebaseToken = firebaseService.verifyIdToken(request.getIdToken());
    String phoneNumber = firebaseService.extractPhoneNumber(firebaseToken);

    // Check if user already exists
    if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
      throw new AuthenticationException("E_AUTH005"); // User already exists
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
            .providerId(firebaseToken.getUid())
            .phoneVerified(true)
            .emailVerified(false)
            .roles(getDefaultRoles())
            .build();

    User savedUser = userRepository.save(newUser);
    log.info("New user registered via phone: {}", phoneNumber);

    // Generate JWT tokens
    String accessToken = jwtTokenProvider.generateTokenFromUser(savedUser);
    String refreshToken = createRefreshToken(savedUser);

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtExpirationMs / 1000)
        .build();
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
      throw new AuthenticationException("E_AUTH006"); // Invalid or expired OTP
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

      return EmailLoginResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .expiresIn(jwtExpirationMs / 1000)
          .isNewUser(false)
          .otpVerified(true)
          .build();
    } else {
      // New user - return flag to complete registration
      log.info("New email user detected, needs registration: {}", email);

      return EmailLoginResponse.builder().isNewUser(true).otpVerified(true).build();
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
    String email = request.getEmail().trim().toLowerCase();

    // Verify OTP again for security
    if (!emailOtpService.verifyOtp(email, request.getOtp())) {
      throw new AuthenticationException("E_AUTH006"); // Invalid or expired OTP
    }

    // Check if user already exists
    if (userRepository.findByEmail(email).isPresent()) {
      throw new AuthenticationException("E_AUTH005"); // User already exists
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

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtExpirationMs / 1000)
        .build();
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

    return AuthResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshTokenValue)
        .tokenType("Bearer")
        .expiresIn(jwtExpirationMs / 1000)
        .build();
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
