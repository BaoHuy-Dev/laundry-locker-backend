package com.huynqb.laundrylockerbackend.module.partner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request for staff to unlock a box using access code. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffCodeUnlockRequest {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotBlank(message = "Access code is required")
  private String accessCode;

  /** Optional: Staff name for tracking. */
  private String staffName;
}
