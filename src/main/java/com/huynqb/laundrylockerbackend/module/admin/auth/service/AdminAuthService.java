package com.huynqb.laundrylockerbackend.module.admin.auth.service;

import static com.huynqb.laundrylockerbackend.core.constant.MessageConstants.*;

import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.admin.auth.dto.request.AdminLoginRequest;
import com.huynqb.laundrylockerbackend.module.admin.auth.dto.request.AdminVerify2faRequest;
import com.huynqb.laundrylockerbackend.module.admin.auth.dto.response.Admin2faResponse;
import com.huynqb.laundrylockerbackend.module.admin.auth.dto.response.AdminLoginResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.RefreshTokenRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.exception.AuthenticationException;
import com.huynqb.laundrylockerbackend.module.auth.service.EmailOtpService;
import com.huynqb.laundrylockerbackend.module.auth.service.TokenService;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AdminAuthService - Handles admin authentication with password + 2FA (Email OTP).
 *
 * <p>Flow:
 *
 * <ol>
 *   <li>Admin submits email + password
 *   <li>System verifies credentials and sends OTP to email
 *   <li>Admin submits OTP code
 *   <li>System verifies OTP and issues JWT tokens
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

  private static final String ADMIN_2FA_TOKEN_PREFIX = "admin_2fa:";
  private static final String ADMIN_LOGIN_ATTEMPTS_PREFIX = "admin_login_attempts:";
  private static final int MAX_LOGIN_ATTEMPTS = 5;
  private static final int LOCKOUT_DURATION_MINUTES = 30;
  private static final int TEMP_TOKEN_EXPIRATION_MINUTES = 10;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailOtpService emailOtpService;
  private final TokenService tokenService;
  private final JwtTokenProvider jwtTokenProvider;
  private final StringRedisTemplate redisTemplate;

  @Value("${app.security.jwt.expiration-ms}")
  private long jwtExpirationMs;

  @Value("${app.security.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  /**
   * Step 1: Verify admin credentials (email + password) and send 2FA OTP.
   *
   * @param request Admin login request with email and password
   * @return Response with temp token for 2FA verification
   */
  @Transactional
  public AdminLoginResponse login(AdminLoginRequest request) {
    String email = request.getEmail().trim().toLowerCase();

    // Check if account is locked
    if (isAccountLocked(email)) {
      log.warn("Admin login attempt on locked account: {}", email);
      throw new AuthenticationException(E_ADMIN_AUTH_LOCKED);
    }

    // Find user by email
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> {
                  incrementLoginAttempts(email);
                  log.warn("Admin login failed - email not found: {}", email);
                  return new AuthenticationException(E_ADMIN_AUTH_INVALID_CREDENTIALS);
                });

    // Check if user has ADMIN role
    boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ADMIN);

    if (!isAdmin) {
      incrementLoginAttempts(email);
      log.warn("Non-admin user attempted admin login: {}", email);
      throw new AuthenticationException(E_ADMIN_AUTH_NOT_ADMIN);
    }

    // Verify password
    if (user.getPassword() == null
        || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      incrementLoginAttempts(email);
      log.warn("Admin login failed - wrong password: {}", email);
      throw new AuthenticationException(E_ADMIN_AUTH_INVALID_CREDENTIALS);
    }

    // Check if account is enabled
    if (!user.getEnabled()) {
      log.warn("Admin login failed - account disabled: {}", email);
      throw new AuthenticationException(E_ADMIN_AUTH_ACCOUNT_DISABLED);
    }

    // Clear login attempts on successful password verification
    clearLoginAttempts(email);

    // Generate temp token for 2FA
    String tempToken = "admin_2fa_" + UUID.randomUUID().toString();

    // Store temp token in Redis with user ID
    String key = ADMIN_2FA_TOKEN_PREFIX + tempToken;
    redisTemplate
        .opsForValue()
        .set(key, user.getId().toString(), TEMP_TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);

    // Send OTP to email
    boolean otpSent = emailOtpService.sendOtp(email);
    if (!otpSent) {
      log.error("Failed to send OTP email to admin: {}", email);
      throw new AuthenticationException(E_ADMIN_AUTH_OTP_SEND_FAILED);
    }

    log.info("Admin login step 1 successful, OTP sent to: {}", email);

    return AdminLoginResponse.builder()
        .requiresTwoFactor(true)
        .tempToken(tempToken)
        .expiresIn((long) TEMP_TOKEN_EXPIRATION_MINUTES * 60)
        .maskedEmail(maskEmail(email))
        .message("OTP has been sent to your email")
        .build();
  }

  /**
   * Step 2: Verify 2FA OTP and issue JWT tokens.
   *
   * @param request 2FA verification request with temp token and OTP
   * @return Response with JWT access and refresh tokens
   */
  @Transactional
  public Admin2faResponse verify2fa(AdminVerify2faRequest request) {
    String tempToken = request.getTempToken();

    // Get user ID from temp token
    String key = ADMIN_2FA_TOKEN_PREFIX + tempToken;
    String userIdStr = redisTemplate.opsForValue().get(key);

    if (userIdStr == null) {
      log.warn(
          "Invalid or expired temp token: {}",
          tempToken.substring(0, Math.min(20, tempToken.length())));
      throw new AuthenticationException(E_ADMIN_AUTH_TEMP_TOKEN_INVALID);
    }

    Long userId = Long.parseLong(userIdStr);

    // Find user
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new AuthenticationException(E_ADMIN_AUTH_TEMP_TOKEN_INVALID));

    // Verify OTP
    if (!emailOtpService.verifyOtp(user.getEmail(), request.getOtpCode())) {
      log.warn("Invalid OTP for admin: {}", user.getEmail());
      throw new AuthenticationException(E_ADMIN_AUTH_OTP_INVALID);
    }

    // Delete temp token after successful verification
    redisTemplate.delete(key);

    // Generate JWT tokens
    String accessToken = jwtTokenProvider.generateTokenFromUser(user);
    String refreshToken = createRefreshToken(user);

    // Extract role names
    Set<String> roleNames =
        user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(java.util.stream.Collectors.toSet());

    log.info("Admin 2FA verification successful for: {}", user.getEmail());

    return Admin2faResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtExpirationMs / 1000)
        .roles(roleNames)
        .user(
            Admin2faResponse.AdminUserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(
                    user.getName() != null
                        ? user.getName()
                        : (user.getFirstName() + " " + user.getLastName()))
                .roles(roleNames)
                .build())
        .build();
  }

  /**
   * Refresh admin access token.
   *
   * @param request Refresh token request
   * @return New access token
   */
  @Transactional
  public AuthResponse refreshToken(RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();

    // Validate refresh token and get user ID
    Long userId = tokenService.getUserIdByRefreshToken(refreshToken);
    if (userId == null) {
      throw new AuthenticationException(E_AUTH006);
    }

    // Find user and verify still has ADMIN role
    User user =
        userRepository.findById(userId).orElseThrow(() -> new AuthenticationException(E_AUTH006));

    boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ADMIN);

    if (!isAdmin) {
      throw new AuthenticationException(E_ADMIN_AUTH_NOT_ADMIN);
    }

    // Generate new access token
    String accessToken = jwtTokenProvider.generateTokenFromUser(user);

    // Extract role names
    Set<String> roleNames =
        user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(java.util.stream.Collectors.toSet());

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresIn(jwtExpirationMs / 1000)
        .roles(roleNames)
        .build();
  }

  // ==================== Helper Methods ====================

  /** Create and store refresh token for user. */
  private String createRefreshToken(User user) {
    String refreshToken = UUID.randomUUID().toString();
    tokenService.saveRefreshToken(refreshToken, user.getId(), refreshExpirationMs);
    return refreshToken;
  }

  /** Check if account is locked due to too many failed attempts. */
  private boolean isAccountLocked(String email) {
    String key = ADMIN_LOGIN_ATTEMPTS_PREFIX + email;
    String attempts = redisTemplate.opsForValue().get(key);
    return attempts != null && Integer.parseInt(attempts) >= MAX_LOGIN_ATTEMPTS;
  }

  /** Increment failed login attempts counter. */
  private void incrementLoginAttempts(String email) {
    String key = ADMIN_LOGIN_ATTEMPTS_PREFIX + email;
    Long attempts = redisTemplate.opsForValue().increment(key);

    if (attempts != null && attempts == 1) {
      // Set expiration on first attempt
      redisTemplate.expire(key, LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
    }

    log.debug("Login attempts for {}: {}", email, attempts);
  }

  /** Clear login attempts after successful authentication. */
  private void clearLoginAttempts(String email) {
    String key = ADMIN_LOGIN_ATTEMPTS_PREFIX + email;
    redisTemplate.delete(key);
  }

  /** Mask email for security display (e.g., ad***@example.com). */
  private String maskEmail(String email) {
    int atIndex = email.indexOf('@');
    if (atIndex <= 2) {
      return "***" + email.substring(atIndex);
    }
    return email.substring(0, 2) + "***" + email.substring(atIndex);
  }
}
