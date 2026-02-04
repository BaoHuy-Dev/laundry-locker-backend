package com.huynqb.laundrylockerbackend.module.loyalty.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for loyalty account information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyAccountResponse {

  private Long id;
  private Long userId;
  private String userName;

  /** Current points balance. */
  private Long pointsBalance;

  /** Points value in VND (1 point = 1 VND). */
  private BigDecimal pointsValueVnd;

  /** Total points earned all time. */
  private Long totalPointsEarned;

  /** Total points redeemed. */
  private Long totalPointsRedeemed;

  /** Total amount spent. */
  private BigDecimal totalAmountSpent;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
