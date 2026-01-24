package com.huynqb.laundrylockerbackend.module.loyalty.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for loyalty summary (combined points + stamps). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltySummaryResponse {

  /** Points information. */
  private LoyaltyAccountResponse pointsAccount;

  /** List of stamp cards. */
  private List<StampCardResponse> stampCards;

  /** Total value in VND that can be redeemed. */
  private BigDecimal totalRedeemableValue;

  /** Number of free rewards available across all cards. */
  private Integer totalFreeRewards;
}
