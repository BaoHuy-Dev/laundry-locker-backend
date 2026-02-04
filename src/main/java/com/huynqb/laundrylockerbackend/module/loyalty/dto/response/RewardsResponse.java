package com.huynqb.laundrylockerbackend.module.loyalty.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for available rewards. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Available rewards list")
public class RewardsResponse {

  @Schema(description = "User's current points balance")
  private Integer currentPoints;

  @Schema(description = "User's membership tier")
  private String membershipTier;

  @Schema(description = "Points needed for next tier")
  private Integer pointsToNextTier;

  @Schema(description = "List of available rewards")
  private List<RewardItem> availableRewards;

  @Schema(description = "List of redeemed rewards")
  private List<RedeemedReward> redeemedRewards;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Reward item")
  public static class RewardItem {

    @Schema(description = "Reward ID")
    private Long id;

    @Schema(description = "Reward name")
    private String name;

    @Schema(description = "Reward description")
    private String description;

    @Schema(description = "Points required to redeem")
    private Integer pointsRequired;

    @Schema(description = "Reward type: DISCOUNT, FREE_SERVICE, VOUCHER, MERCHANDISE")
    private String type;

    @Schema(description = "Reward value (discount amount or service ID)")
    private String value;

    @Schema(description = "Reward image URL")
    private String imageUrl;

    @Schema(description = "Whether user can afford this reward")
    private Boolean canRedeem;

    @Schema(description = "Remaining quantity (null = unlimited)")
    private Integer remainingQuantity;

    @Schema(description = "Expiration date for the reward offer")
    private LocalDateTime expiresAt;

    @Schema(description = "Minimum tier required")
    private String minimumTier;

    @Schema(description = "Category of reward")
    private String category;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Redeemed reward")
  public static class RedeemedReward {

    @Schema(description = "Redemption ID")
    private Long id;

    @Schema(description = "Reward name")
    private String rewardName;

    @Schema(description = "Points spent")
    private Integer pointsSpent;

    @Schema(description = "When redeemed")
    private LocalDateTime redeemedAt;

    @Schema(description = "Redemption code")
    private String code;

    @Schema(description = "Status: ACTIVE, USED, EXPIRED")
    private String status;

    @Schema(description = "When the redemption expires")
    private LocalDateTime expiresAt;

    @Schema(description = "When it was used (if applicable)")
    private LocalDateTime usedAt;
  }
}
