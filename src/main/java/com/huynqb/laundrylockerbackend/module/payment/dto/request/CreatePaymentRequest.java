package com.huynqb.laundrylockerbackend.module.payment.dto.request;

import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for creating an online payment. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "Payment method is required")
  private PaymentMethod paymentMethod;

  /** Bank code for VNPay (optional). If empty, user selects bank on VNPay page */
  private String bankCode;

  /** Language for payment page (vn or en). Default: vn */
  @Builder.Default private String language = "vn";
}
