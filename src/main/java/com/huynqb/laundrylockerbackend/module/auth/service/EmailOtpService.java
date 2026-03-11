package com.huynqb.laundrylockerbackend.module.auth.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email OTP Service - Handles OTP generation, storage (Redis), and verification. Follows Single
 * Responsibility Principle: Only handles OTP operations for email authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOtpService {

  private static final String OTP_KEY_PREFIX = "email_otp:";
  private static final int OTP_LENGTH = 6;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final StringRedisTemplate redisTemplate;
  private final JavaMailSender mailSender;

  @Value("${app.otp.expiration-minutes:5}")
  private int otpExpirationMinutes;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${app.name:Laundry Locker}")
  private String appName;

  /**
   * Generate and send OTP to email. Stores OTP in Redis with TTL.
   *
   * @param email Target email address
   * @return true if sent successfully
   */
  public boolean sendOtp(String email) {
    String otp = generateOtp();
    String key = OTP_KEY_PREFIX + email;

    // Store OTP in Redis with TTL
    redisTemplate.opsForValue().set(key, otp, otpExpirationMinutes, TimeUnit.MINUTES);
    log.info("OTP stored in Redis for email: {}", email);

    // Send email
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(email);
      message.setSubject("Mã OTP đăng nhập " + appName);
      message.setText(
          String.format(
              "Mã OTP của bạn là: %s\n\nMã này có hiệu lực trong %d phút.\n\nNếu bạn không yêu cầu mã này, vui lòng bỏ qua email.",
              otp, otpExpirationMinutes));

      mailSender.send(message);
      log.info("OTP email sent successfully to: {}", email);
      return true;

    } catch (Exception e) {
      log.error("Failed to send OTP email to {}: {}", email, e.getMessage());
      // Delete OTP from Redis if email fails
      redisTemplate.delete(key);
      throw new com.huynqb.laundrylockerbackend.module.auth.exception.AuthenticationException(
          "Gửi email thất bại (" + e.getMessage() + ").");
    }
  }

  /**
   * Verify OTP for email.
   *
   * @param email Email address
   * @param otp OTP code to verify
   * @return true if OTP is valid
   */
  public boolean verifyOtp(String email, String otp) {
    String key = OTP_KEY_PREFIX + email;
    String storedOtp = redisTemplate.opsForValue().get(key);

    if (storedOtp == null) {
      log.warn("OTP not found or expired for email: {}", email);
      return false;
    }

    if (storedOtp.equals(otp)) {
      // Delete OTP after successful verification (one-time use)
      redisTemplate.delete(key);
      log.info("OTP verified successfully for email: {}", email);
      return true;
    }

    log.warn("Invalid OTP attempt for email: {}", email);
    return false;
  }

  /**
   * Check if OTP exists for email (not expired).
   *
   * @param email Email address
   * @return true if OTP exists
   */
  public boolean hasActiveOtp(String email) {
    String key = OTP_KEY_PREFIX + email;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  /**
   * Generate secure random OTP.
   *
   * @return 6-digit OTP string
   */
  private String generateOtp() {
    int otp = SECURE_RANDOM.nextInt((int) Math.pow(10, OTP_LENGTH));
    return String.format("%0" + OTP_LENGTH + "d", otp);
  }
}
