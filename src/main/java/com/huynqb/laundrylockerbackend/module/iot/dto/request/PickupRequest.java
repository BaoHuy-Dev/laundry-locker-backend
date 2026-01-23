package com.huynqb.laundrylockerbackend.module.iot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for customer pickup confirmation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickupRequest {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "Box ID is required")
  private Long boxId;
}
