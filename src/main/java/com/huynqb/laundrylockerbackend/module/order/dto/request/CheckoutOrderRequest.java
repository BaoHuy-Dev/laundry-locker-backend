package com.huynqb.laundrylockerbackend.module.order.dto.request;

import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for checkout order. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutOrderRequest {

  @NotNull(message = "Payment method is required")
  private PaymentMethod paymentMethod;

  private String note;
}
