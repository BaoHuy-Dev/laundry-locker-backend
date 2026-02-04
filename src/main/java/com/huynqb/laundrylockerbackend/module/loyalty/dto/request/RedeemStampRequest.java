package com.huynqb.laundrylockerbackend.module.loyalty.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for redeeming stamp reward. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedeemStampRequest {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "Stamp card ID is required")
  private Long stampCardId;

  /**
   * For BOX type: the box size to apply free reward. For SERVICE type: the service ID to apply free
   * reward.
   */
  private String boxSize;

  private Long serviceId;
}
