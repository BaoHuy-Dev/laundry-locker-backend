package com.huynqb.laundrylockerbackend.module.order.enums;

/**
 * Pricing type enum for orders.
 */
public enum PricingType {
  
  /**
   * Fixed price - known at order creation.
   * Used for STORAGE/drop-off services.
   */
  FIXED,
  
  /**
   * Per-weight pricing - estimated at creation, final after weighing.
   * Used for LAUNDRY services charged per kg.
   */
  PER_WEIGHT,
  
  /**
   * Per-piece pricing - estimated at creation, final after counting.
   * Used for dry cleaning, ironing per item.
   */
  PER_PIECE
}
