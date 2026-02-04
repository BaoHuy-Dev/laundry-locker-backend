package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for promotion information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Promotion information")
public class PromotionResponse {

  @Schema(description = "Promotion ID")
  private Long id;

  @Schema(description = "Unique promotion code")
  private String code;

  @Schema(description = "Promotion title")
  private String title;

  @Schema(description = "Promotion description")
  private String description;

  @Schema(description = "Discount type: PERCENTAGE, FIXED_AMOUNT, FREE_SERVICE")
  private String discountType;

  @Schema(description = "Discount value")
  private BigDecimal discountValue;

  @Schema(description = "Maximum discount amount")
  private BigDecimal maxDiscountAmount;

  @Schema(description = "Minimum order amount")
  private BigDecimal minOrderAmount;

  @Schema(description = "Promotion start date")
  private LocalDateTime startDate;

  @Schema(description = "Promotion end date")
  private LocalDateTime endDate;

  @Schema(description = "Total usage limit")
  private Integer totalUsageLimit;

  @Schema(description = "Current usage count")
  private Integer currentUsageCount;

  @Schema(description = "Remaining uses")
  private Integer remainingUses;

  @Schema(description = "Per user limit")
  private Integer perUserLimit;

  @Schema(description = "List of applicable service IDs")
  private List<Long> applicableServiceIds;

  @Schema(description = "List of applicable store IDs")
  private List<Long> applicableStoreIds;

  @Schema(description = "List of applicable user tiers")
  private List<String> applicableTiers;

  @Schema(description = "Whether the promotion is active")
  private Boolean isActive;

  @Schema(description = "Priority for stacking")
  private Integer priority;

  @Schema(description = "Whether this can be combined with other promotions")
  private Boolean stackable;

  @Schema(description = "Current status: UPCOMING, ACTIVE, EXPIRED, DEPLETED")
  private String status;

  @Schema(description = "Created at timestamp")
  private LocalDateTime createdAt;

  @Schema(description = "Last updated timestamp")
  private LocalDateTime updatedAt;

  @Schema(description = "Created by admin ID")
  private Long createdBy;
}
