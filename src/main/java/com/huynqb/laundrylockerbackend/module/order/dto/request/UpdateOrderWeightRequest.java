package com.huynqb.laundrylockerbackend.module.order.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for staff to update order weight and services after collection. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderWeightRequest {

  /** Actual weight of the laundry (in kg). */
  @NotNull(message = "Actual weight is required")
  @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
  private BigDecimal actualWeight;

  /** Weight unit (default: kg) */
  @Builder.Default private String weightUnit = "kg";

  /** Updated service items with quantities. */
  private List<OrderItemRequest> items;

  /** Staff note about the order. */
  private String staffNote;
}
