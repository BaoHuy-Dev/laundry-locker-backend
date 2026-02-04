package com.huynqb.laundrylockerbackend.module.staff.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for staff to unlock a box using master PIN. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffUnlockBoxRequest {

  @NotNull(message = "Box ID is required")
  private Long boxId;

  @NotBlank(message = "Master PIN is required")
  private String masterPin;

  /** Optional order ID if unlocking for a specific order. */
  private Long orderId;

  /** Purpose: COLLECT (lấy đồ), RETURN (trả đồ), MAINTENANCE (bảo trì) */
  private String purpose;
}
