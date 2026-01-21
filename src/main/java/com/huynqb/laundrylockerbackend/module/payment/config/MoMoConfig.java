package com.huynqb.laundrylockerbackend.module.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** MoMo configuration properties. Loaded from application properties with prefix 'momo'. */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "momo")
public class MoMoConfig {

  /** MoMo partner code */
  private String partnerCode;

  /** Access key for authentication */
  private String accessKey;

  /** Secret key for HMAC signing */
  private String secretKey;

  /** MoMo API endpoint */
  private String endpoint;

  /** Redirect URL after payment */
  private String redirectUrl;

  /** IPN URL for callback */
  private String ipnUrl;

  /** Request type (default: captureWallet) */
  private String requestType = "captureWallet";

  /** Payment expiration in minutes (default: 15) */
  private int expireMinutes = 15;
}
