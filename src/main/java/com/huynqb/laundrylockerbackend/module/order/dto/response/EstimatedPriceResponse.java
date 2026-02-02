package com.huynqb.laundrylockerbackend.module.order.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for estimated price information.
 * Used for LAUNDRY orders where final price is determined after weighing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimatedPriceResponse {
  
  /** Minimum estimated price. */
  private BigDecimal minPrice;
  
  /** Maximum estimated price. */
  private BigDecimal maxPrice;
  
  /** Estimated weight in kg (if applicable). */
  private BigDecimal estimatedWeight;
  
  /** Note explaining the estimate. */
  private String note;
}
