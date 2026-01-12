package com.huynqb.laundrylockerbackend.core.email;

/**
 * Email Service Interface Abstraction for sending emails - can be implemented with SMTP, SendGrid,
 * AWS SES, etc.
 */
public interface EmailService {

  /**
   * Send a simple text email
   *
   * @param to Recipient email address
   * @param subject Email subject
   * @param text Email body (plain text)
   */
  void sendSimpleEmail(String to, String subject, String text);

  /**
   * Send an HTML email
   *
   * @param to Recipient email address
   * @param subject Email subject
   * @param htmlContent Email body (HTML)
   */
  void sendHtmlEmail(String to, String subject, String htmlContent);

  /**
   * Send email verification email
   *
   * @param to Recipient email address
   * @param name User's name
   * @param verificationLink Verification URL
   */
  void sendVerificationEmail(String to, String name, String verificationLink);

  /**
   * Send password reset email
   *
   * @param to Recipient email address
   * @param name User's name
   * @param resetLink Password reset URL
   */
  void sendPasswordResetEmail(String to, String name, String resetLink);
}
