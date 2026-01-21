package com.huynqb.laundrylockerbackend.module.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for order item (service selection). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

  @NotNull(message = "Service ID is required")
  private Long serviceId;

  @Positive(message = "Quantity must be positive")
  private Double quantity;

  private String description;
}
