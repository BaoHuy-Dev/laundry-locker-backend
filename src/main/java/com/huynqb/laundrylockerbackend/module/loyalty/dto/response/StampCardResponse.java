package com.huynqb.laundrylockerbackend.module.loyalty.dto.response;

import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for stamp card information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampCardResponse {

  private Long id;
  private Long userId;
  private StampType stampType;

  /** Service ID if type is SERVICE. */
  private Long serviceId;

  private String serviceName;

  /** Box size if type is BOX. */
  private String boxSize;

  /** Number of stamps required for 1 free reward. */
  private Integer stampsRequired;

  /** Current stamps collected. */
  private Integer currentStamps;

  /** Free rewards available to use. */
  private Integer freeRewardsAvailable;

  /** Total stamps earned all time. */
  private Integer totalStampsEarned;

  /** Total rewards redeemed. */
  private Integer totalRewardsRedeemed;

  /** Progress percentage to next reward. */
  private Integer progressPercentage;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
