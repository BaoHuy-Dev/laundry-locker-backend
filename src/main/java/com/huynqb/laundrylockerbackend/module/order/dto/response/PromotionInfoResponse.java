package com.huynqb.laundrylockerbackend.module.order.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for promotion information applied to an order. Contains details about the promotion
 * and the calculated discount.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionInfoResponse {

  /** Promotion code applied. */
  private String code;

  /** Promotion title/name. */
  private String title;

  /** Promotion description. */
  private String description;

  /** Discount type: PERCENTAGE, FIXED_AMOUNT, FREE_SERVICE. */
  private String discountType;

  /** Discount value (percentage or fixed amount). */
  private BigDecimal discountValue;

  /** Maximum discount amount (for percentage discounts). */
  private BigDecimal maxDiscountAmount;

  /** Minimum order amount required. */
  private BigDecimal minOrderAmount;

  /** Calculated discount amount for this order. */
  private BigDecimal calculatedDiscount;

  /** Whether the promotion was successfully applied. */
  private Boolean applied;

  /** Message explaining the promotion status. */
  private String message;

  /** Whether this promotion can be combined with others. */
  private Boolean stackable;
}
