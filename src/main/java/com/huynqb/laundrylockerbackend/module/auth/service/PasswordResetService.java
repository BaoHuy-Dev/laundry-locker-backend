package com.huynqb.laundrylockerbackend.module.auth.service;

import com.huynqb.laundrylockerbackend.core.email.EmailService;
import com.huynqb.laundrylockerbackend.module.auth.exception.AuthenticationException;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
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
 * Password Reset Service Handles password reset token creation, validation, and password update.
 * Tokens are stored in Redis with TTL for automatic expiration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

  private final StringRedisTemplate redisTemplate;
  private final EmailService emailService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  private static final String RESET_PREFIX = "password:reset:";

  @Value("${app.email.reset-password-expiration-ms:3600000}")
  private long resetExpirationMs; // 1 hour

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  /**
   * Initiate password reset - generate token and send email
   *
   * @param email User's email address
   * @return true if email sent (always return true to prevent email enumeration)
   */
  public boolean initiatePasswordReset(String email) {
    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
      // Don't reveal if email exists or not (security best practice)
      log.info("Password reset requested for non-existent email: {}", email);
      return true;
    }

    // Check if user is OAuth2 user (no password)
    if (user.getPassword() == null || user.getPassword().isEmpty()) {
      log.info("Password reset requested for OAuth2 user: {}", email);
      // Still return true to prevent enumeration
      return true;
    }

    String token = UUID.randomUUID().toString();
    String key = RESET_PREFIX + token;

    // Save token -> email in Redis with TTL
    redisTemplate.opsForValue().set(key, user.getEmail(), resetExpirationMs, TimeUnit.MILLISECONDS);

    // Build reset link (frontend page)
    String resetLink = frontendUrl + "/reset-password?token=" + token;

    // Send email
    emailService.sendPasswordResetEmail(
        user.getEmail(), user.getName() != null ? user.getName() : "User", resetLink);

    log.info("Password reset email sent to: {}", user.getEmail());
    return true;
  }

  /**
   * Reset password with token
   *
   * @param token Reset token
   * @param newPassword New password
   * @return true if password reset successfully
   */
  @Transactional
  public boolean resetPassword(String token, String newPassword) {
    String key = RESET_PREFIX + token;
    String email = redisTemplate.opsForValue().get(key);

    if (email == null) {
      log.warn("Invalid or expired password reset token");
      throw new AuthenticationException("E_AUTH006"); // Invalid or expired token
    }

    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
      log.error("User not found for password reset: {}", email);
      throw new AuthenticationException("E_AUTH006");
    }

    // Validate password strength (basic validation)
    if (newPassword == null || newPassword.length() < 6) {
      throw new AuthenticationException("E_VALIDATION001"); // Password too short
    }

    // Update password
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    // Delete used token
    redisTemplate.delete(key);

    log.info("Password reset successfully for user: {}", email);
    return true;
  }

  /**
   * Validate reset token (for frontend to check before showing form)
   *
   * @param token Reset token
   * @return true if token is valid
   */
  public boolean validateResetToken(String token) {
    String key = RESET_PREFIX + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }
}
