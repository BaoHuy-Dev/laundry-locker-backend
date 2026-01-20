package com.huynqb.laundrylockerbackend.module.payment.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO containing payment URL for redirect. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentUrlResponse {

  /** Payment ID in our system */
  private Long paymentId;

  /** Order ID */
  private Long orderId;

  /** URL to redirect user for payment */
  private String paymentUrl;

  /** Payment expiration time */
  private LocalDateTime expireAt;

  /** QR code URL (for MoMo) */
  private String qrCodeUrl;

  /** Deeplink for mobile app (for MoMo) */
  private String deeplink;
}
