package com.huynqb.laundrylockerbackend.module.laundry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for price estimation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Service price estimation request")
public class ServiceEstimateRequest {

  @NotEmpty(message = "At least one service is required")
  @Schema(description = "List of service IDs to estimate")
  private List<Long> serviceIds;

  @Min(value = 1, message = "Quantity must be at least 1")
  @Schema(description = "Quantity or weight (in kg for weight-based)", example = "5")
  @Builder.Default
  private Integer quantity = 1;

  @Schema(description = "Store ID for store-specific pricing")
  private Long storeId;

  @Schema(description = "Partner ID for partner-specific pricing")
  private Long partnerId;

  @Schema(description = "Promo code to apply")
  private String promoCode;

  @Schema(description = "Express service flag", example = "false")
  @Builder.Default
  private Boolean isExpress = false;

  @Schema(description = "User's membership tier for discount calculation", example = "GOLD")
  private String membershipTier;
}
