package com.huynqb.laundrylockerbackend.module.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** VNPay configuration properties. Loaded from application properties with prefix 'vnpay'. */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {

  /** VNPay merchant code (TmnCode) */
  private String tmnCode;

  /** Secret key for HMAC signing */
  private String hashSecret;

  /** VNPay payment gateway URL */
  private String payUrl;

  /** Return URL after payment completion */
  private String returnUrl;

  /** IPN (Instant Payment Notification) URL for server-to-server callback */
  private String ipnUrl;

  /** API version (default: 2.1.0) */
  private String version = "2.1.0";

  /** Currency code (default: VND) */
  private String currCode = "VND";

  /** Payment command (default: pay) */
  private String command = "pay";

  /** Payment expiration in minutes (default: 15) */
  private int expireMinutes = 15;
}
