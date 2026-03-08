package com.huynqb.laundrylockerbackend.module.partner.dto.request;

import jakarta.validation.constraints.NotBlank;
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

  /** Optional: If null, the order will be derived from the access code. */
  private Long orderId;

  @NotBlank(message = "Access code is required")
  private String accessCode;

  /** Optional: Staff name for tracking. */
  private String staffName;
}
