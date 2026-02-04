package com.huynqb.laundrylockerbackend.module.partner.dto.request;

import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request to generate a staff access code. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateAccessCodeRequest {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "Action is required (COLLECT or RETURN)")
  private AccessCodeAction action;

  /** Optional: Code expiration in hours (default: 24 hours). */
  private Integer expirationHours;

  /** Optional: Notes for the staff. */
  private String notes;
}
