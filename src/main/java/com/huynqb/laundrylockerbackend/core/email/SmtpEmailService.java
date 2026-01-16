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

/** SMTP Email Service Implementation Sends emails via configured SMTP server (Gmail, etc.) */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:noreply@example.com}")
  private String fromEmail;

  @Value("${app.name:Laundry Locker}")
  private String appName;

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

  @Override
  @Async
  public void sendVerificationEmail(String to, String name, String verificationLink) {
    log.info("📧 Starting verification email to: {}", to);
    String subject = "[" + appName + "] Xác thực email của bạn";
    String htmlContent = buildVerificationEmailTemplate(name, verificationLink);
    sendHtmlEmail(to, subject, htmlContent);
  }

  @Override
  @Async
  public void sendPasswordResetEmail(String to, String name, String resetLink) {
    log.info("📧 Starting password reset email to: {}", to);
    String subject = "[" + appName + "] Đặt lại mật khẩu";
    String htmlContent = buildPasswordResetEmailTemplate(name, resetLink);
    sendHtmlEmail(to, subject, htmlContent);
  }

  private String buildVerificationEmailTemplate(String name, String verificationLink) {
    // Use String concatenation to avoid conflicts with # in CSS colors
    return "<!DOCTYPE html>"
        + "<html>"
        + "<head>"
        + "    <meta charset=\"UTF-8\">"
        + "    <style>"
        + "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
        + "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }"
        + "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }"
        + "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }"
        + "        .button { display: inline-block; background: #667eea; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }"
        + "        .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }"
        + "    </style>"
        + "</head>"
        + "<body>"
        + "    <div class=\"container\">"
        + "        <div class=\"header\">"
        + "            <h1>🧺 "
        + appName
        + "</h1>"
        + "        </div>"
        + "        <div class=\"content\">"
        + "            <h2>Xin chào "
        + name
        + "!</h2>"
        + "            <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng click vào nút bên dưới để xác thực email của bạn:</p>"
        + "            <p style=\"text-align: center;\">"
        + "                <a href=\""
        + verificationLink
        + "\" class=\"button\">Xác thực Email</a>"
        + "            </p>"
        + "            <p>Hoặc copy đường link sau vào trình duyệt:</p>"
        + "            <p style=\"word-break: break-all; color: #667eea;\">"
        + verificationLink
        + "</p>"
        + "            <p><strong>Lưu ý:</strong> Link này sẽ hết hạn sau 24 giờ.</p>"
        + "        </div>"
        + "        <div class=\"footer\">"
        + "            <p>Email này được gửi tự động từ "
        + appName
        + ". Vui lòng không trả lời.</p>"
        + "        </div>"
        + "    </div>"
        + "</body>"
        + "</html>";
  }

  private String buildPasswordResetEmailTemplate(String name, String resetLink) {
    return "<!DOCTYPE html>"
        + "<html>"
        + "<head>"
        + "    <meta charset=\"UTF-8\">"
        + "    <style>"
        + "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
        + "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }"
        + "        .header { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }"
        + "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }"
        + "        .button { display: inline-block; background: #f5576c; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }"
        + "        .warning { background: #fff3cd; border: 1px solid #ffc107; padding: 15px; border-radius: 5px; margin: 15px 0; }"
        + "        .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }"
        + "    </style>"
        + "</head>"
        + "<body>"
        + "    <div class=\"container\">"
        + "        <div class=\"header\">"
        + "            <h1>🔐 Đặt lại mật khẩu</h1>"
        + "        </div>"
        + "        <div class=\"content\">"
        + "            <h2>Xin chào "
        + name
        + "!</h2>"
        + "            <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Click vào nút bên dưới để tiếp tục:</p>"
        + "            <p style=\"text-align: center;\">"
        + "                <a href=\""
        + resetLink
        + "\" class=\"button\">Đặt lại mật khẩu</a>"
        + "            </p>"
        + "            <div class=\"warning\">"
        + "                <strong>⚠️ Lưu ý:</strong>"
        + "                <ul>"
        + "                    <li>Link này sẽ hết hạn sau 1 giờ.</li>"
        + "                    <li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</li>"
        + "                </ul>"
        + "            </div>"
        + "            <p>Hoặc copy đường link sau vào trình duyệt:</p>"
        + "            <p style=\"word-break: break-all; color: #f5576c;\">"
        + resetLink
        + "</p>"
        + "        </div>"
        + "        <div class=\"footer\">"
        + "            <p>Email này được gửi tự động từ "
        + appName
        + ". Vui lòng không trả lời.</p>"
        + "        </div>"
        + "    </div>"
        + "</body>"
        + "</html>";
  }
}
