package com.huynqb.laundrylockerbackend.module.order.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for price breakdown details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdownResponse {
  
  /** Base service price. */
  private BigDecimal basePrice;
  
  /** Storage/locker fee. */
  private BigDecimal storageFee;
  
  /** Overtime/late pickup fee. */
  private BigDecimal overtimeFee;
  
  /** Shipping/delivery fee. */
  private BigDecimal shippingFee;
  
  /** Total discount amount (from promotions). */
  private BigDecimal discount;
  
  // ===== PROMOTION BREAKDOWN =====
  
  /** Original price before any discounts. */
  private BigDecimal originalPrice;
  
  /** Promotion code applied. */
  private String promotionCode;
  
  /** Promotion discount amount. */
  private BigDecimal promotionDiscount;
  
  /** List of applied promotions (for multiple). */
  private List<PromotionInfoResponse> appliedPromotions;
  
  /** Final price after all discounts. */
  private BigDecimal finalPrice;
  
  /** Note explaining the breakdown. */
  private String note;
}
