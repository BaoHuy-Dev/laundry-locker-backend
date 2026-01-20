package com.huynqb.laundrylockerbackend.core.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** SMTP Email Service Implementation. Sends emails via configured SMTP server (Gmail, etc.). */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:noreply@example.com}")
  private String fromEmail;

  @Override
  @Async
  public void sendSimpleEmail(String to, String subject, String text) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(text);
      mailSender.send(message);
      log.info("Simple email sent to: {}", to);
    } catch (Exception e) {
      log.error("Failed to send simple email to {}: {}", to, e.getMessage());
    }
  }

  @Override
  public void sendHtmlEmail(String to, String subject, String htmlContent) {
    try {
      log.info("Preparing to send HTML email to: {}", to);
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);
      mailSender.send(message);
      log.info("HTML email SENT successfully to: {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
    } catch (Exception e) {
      log.error("Unexpected error sending email to {}: {}", to, e.getMessage(), e);
    }
  }
}
