package com.huynqb.laundrylockerbackend.module.laundry.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for price estimation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Service price estimation response")
public class ServiceEstimateResponse {

  @Schema(description = "Breakdown of each service cost")
  private List<ServiceCostItem> items;

  @Schema(description = "Subtotal before discounts")
  private BigDecimal subtotal;

  @Schema(description = "Express service fee (if applicable)")
  private BigDecimal expressFee;

  @Schema(description = "Discount amount from promo code")
  private BigDecimal promoDiscount;

  @Schema(description = "Applied promo code")
  private String appliedPromoCode;

  @Schema(description = "Membership discount amount")
  private BigDecimal membershipDiscount;

  @Schema(description = "Total discount")
  private BigDecimal totalDiscount;

  @Schema(description = "Tax amount")
  private BigDecimal tax;

  @Schema(description = "Final total after all discounts and fees")
  private BigDecimal total;

  @Schema(description = "Estimated processing time in hours")
  private Integer estimatedHours;

  @Schema(description = "Currency code", example = "VND")
  @Builder.Default
  private String currency = "VND";

  @Schema(description = "Validity of this estimate in minutes")
  @Builder.Default
  private Integer validForMinutes = 30;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Individual service cost breakdown")
  public static class ServiceCostItem {

    @Schema(description = "Service ID")
    private Long serviceId;

    @Schema(description = "Service name")
    private String serviceName;

    @Schema(description = "Unit price")
    private BigDecimal unitPrice;

    @Schema(description = "Quantity or weight")
    private Integer quantity;

    @Schema(description = "Unit type: PIECE, KG, ITEM")
    private String unitType;

    @Schema(description = "Line total (unitPrice * quantity)")
    private BigDecimal lineTotal;

    @Schema(description = "Processing time in hours")
    private Integer processingHours;
  }
}
