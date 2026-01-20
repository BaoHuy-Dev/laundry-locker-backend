package com.huynqb.laundrylockerbackend.module.payment.dto.response;

import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentMethod;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for payment. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

  private Long id;
  private Long orderId;
  private Long customerId;
  private String customerName;
  private BigDecimal amount;
  private PaymentMethod method;
  private PaymentStatus status;
  private String content;
  private String referenceId;
  private String referenceTransactionId;
  private String qr;
  private String url;
  private String deeplink;
  private String description;
  private LocalDateTime createdAt;
}
