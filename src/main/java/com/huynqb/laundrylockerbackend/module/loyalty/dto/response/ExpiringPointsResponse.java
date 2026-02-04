package com.huynqb.laundrylockerbackend.module.loyalty.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for expiring points information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Expiring points information")
public class ExpiringPointsResponse {

  @Schema(description = "Total points expiring within the notification period")
  private Integer totalExpiringPoints;

  @Schema(description = "User's current points balance")
  private Integer currentBalance;

  @Schema(description = "Breakdown by expiration date")
  private List<ExpiringBatch> expiringBatches;

  @Schema(description = "Recommended actions to use expiring points")
  private List<RecommendedAction> recommendedActions;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Batch of expiring points")
  public static class ExpiringBatch {

    @Schema(description = "Points in this batch")
    private Integer points;

    @Schema(description = "When these points expire")
    private LocalDateTime expiresAt;

    @Schema(description = "Days until expiration")
    private Integer daysUntilExpiration;

    @Schema(description = "Source of these points (order ID, promotion, etc.)")
    private String source;

    @Schema(description = "When these points were earned")
    private LocalDateTime earnedAt;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Recommended action to use points")
  public static class RecommendedAction {

    @Schema(description = "Action type: REDEEM_REWARD, USE_DISCOUNT")
    private String type;

    @Schema(description = "Description of the action")
    private String description;

    @Schema(description = "Points that can be used")
    private Integer pointsToUse;

    @Schema(description = "Reward ID if applicable")
    private Long rewardId;

    @Schema(description = "Action URL or deep link")
    private String actionUrl;
  }
}
