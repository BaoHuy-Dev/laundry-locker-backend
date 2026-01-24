package com.huynqb.laundrylockerbackend.module.loyalty.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for redeeming points. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedeemPointsRequest {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "Points to redeem is required")
  @Min(value = 1, message = "Points must be at least 1")
  private Long pointsToRedeem;
}
