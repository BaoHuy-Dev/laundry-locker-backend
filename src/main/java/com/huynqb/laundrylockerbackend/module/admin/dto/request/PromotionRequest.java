package com.huynqb.laundrylockerbackend.module.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for creating/updating promotions. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Promotion request")
public class PromotionRequest {

  @NotBlank(message = "Promotion code is required")
  @Size(min = 3, max = 20, message = "Code must be between 3 and 20 characters")
  @Pattern(
      regexp = "^[A-Z0-9_]+$",
      message = "Code must contain only uppercase letters, numbers, and underscores")
  @Schema(description = "Unique promotion code", example = "SUMMER2024")
  private String code;

  @NotBlank(message = "Title is required")
  @Size(max = 100, message = "Title cannot exceed 100 characters")
  @Schema(description = "Promotion title", example = "Summer Sale 2024")
  private String title;

  @Size(max = 500, message = "Description cannot exceed 500 characters")
  @Schema(description = "Promotion description")
  private String description;

  @NotNull(message = "Discount type is required")
  @Schema(description = "Discount type: PERCENTAGE, FIXED_AMOUNT, FREE_SERVICE")
  private DiscountType discountType;

  @NotNull(message = "Discount value is required")
  @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
  @Schema(description = "Discount value (percentage or fixed amount)", example = "20")
  private BigDecimal discountValue;

  @Schema(description = "Maximum discount amount (for percentage type)")
  private BigDecimal maxDiscountAmount;

  @Schema(description = "Minimum order amount to apply promotion")
  private BigDecimal minOrderAmount;

  @NotNull(message = "Start date is required")
  @Schema(description = "Promotion start date")
  private LocalDateTime startDate;

  @NotNull(message = "End date is required")
  @Schema(description = "Promotion end date")
  private LocalDateTime endDate;

  @Min(value = 1, message = "Total usage limit must be at least 1")
  @Schema(description = "Total number of times promotion can be used")
  private Integer totalUsageLimit;

  @Min(value = 1, message = "Per user limit must be at least 1")
  @Schema(description = "Number of times each user can use the promotion", example = "1")
  @Builder.Default
  private Integer perUserLimit = 1;

  @Schema(description = "List of applicable service IDs (empty = all services)")
  private List<Long> applicableServiceIds;

  @Schema(description = "List of applicable store IDs (empty = all stores)")
  private List<Long> applicableStoreIds;

  @Schema(description = "List of applicable user tiers (empty = all tiers)")
  private List<String> applicableTiers;

  @Schema(description = "Whether the promotion is active", example = "true")
  @Builder.Default
  private Boolean isActive = true;

  @Schema(description = "Priority for stacking (higher = applied first)", example = "1")
  @Builder.Default
  private Integer priority = 1;

  @Schema(description = "Whether this can be combined with other promotions", example = "false")
  @Builder.Default
  private Boolean stackable = false;

  public enum DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT,
    FREE_SERVICE
  }
}
