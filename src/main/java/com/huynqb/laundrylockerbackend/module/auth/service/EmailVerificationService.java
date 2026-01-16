package com.huynqb.laundrylockerbackend.module.auth.service;

import com.huynqb.laundrylockerbackend.core.email.EmailService;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Email Verification Service Handles email verification token creation, validation, and email
 * sending. Tokens are stored in Redis with TTL for automatic expiration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

  private final StringRedisTemplate redisTemplate;
  private final EmailService emailService;
  private final UserRepository userRepository;

  private static final String VERIFICATION_PREFIX = "email:verify:";

  @Value("${app.email.verification-expiration-ms:86400000}")
  private long verificationExpirationMs; // 24 hours

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  @Value("${app.backend.url:http://localhost:8080}")
  private String backendUrl;

  /**
   * Generate verification token and send verification email
   *
   * @param user The user to verify
   */
  public void sendVerificationEmail(User user) {
    log.info("Creating verification token for user: {}", user.getEmail());

    String token = UUID.randomUUID().toString();
    String key = VERIFICATION_PREFIX + token;

    // Save token -> userId in Redis with TTL
    log.info("Saving token to Redis with key: {}", key);
    redisTemplate
        .opsForValue()
        .set(key, user.getId().toString(), verificationExpirationMs, TimeUnit.MILLISECONDS);
    log.info("Token saved to Redis successfully");

    // Build verification link (backend endpoint)
    String verificationLink = backendUrl + "/api/auth/verify-email?token=" + token;
    log.info("Verification link: {}", verificationLink);

    // Send email
    log.info("Calling emailService.sendVerificationEmail...");
    emailService.sendVerificationEmail(
        user.getEmail(), user.getName() != null ? user.getName() : "User", verificationLink);

    log.info("Verification email request completed for: {}", user.getEmail());
  }

  /**
   * Verify email with token
   *
   * @param token Verification token
   * @return true if verified successfully
   */
  @Transactional
  public boolean verifyEmail(String token) {
    String key = VERIFICATION_PREFIX + token;
    String userIdStr = redisTemplate.opsForValue().get(key);

    if (userIdStr == null) {
      log.warn("Invalid or expired verification token: {}", token);
      return false;
    }

    try {
      Long userId = Long.parseLong(userIdStr);
      User user = userRepository.findById(userId).orElse(null);

      if (user == null) {
        log.warn("User not found for verification token: {}", token);
        return false;
      }

      if (user.getEmailVerified()) {
        log.info("Email already verified for user: {}", user.getEmail());
        // Delete token anyway
        redisTemplate.delete(key);
        return true;
      }

      // Mark email as verified
      user.setEmailVerified(true);
      userRepository.save(user);

      // Delete used token
      redisTemplate.delete(key);

      log.info("Email verified successfully for user: {}", user.getEmail());
      return true;

    } catch (NumberFormatException e) {
      log.error("Invalid userId in verification token: {}", userIdStr);
      return false;
    }
  }

  /**
   * Resend verification email
   *
   * @param email User's email address
   * @return true if email sent successfully
   */
  public boolean resendVerificationEmail(String email) {
    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
      log.warn("User not found for resend verification: {}", email);
      return false;
    }

    if (user.getEmailVerified()) {
      log.info("Email already verified for: {}", email);
      return false;
    }

    sendVerificationEmail(user);
    return true;
  }
}
