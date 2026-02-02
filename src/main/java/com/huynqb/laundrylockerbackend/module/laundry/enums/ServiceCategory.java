package com.huynqb.laundrylockerbackend.module.laundry.enums;

/**
 * Service category enum to distinguish between storage and laundry services.
 * 
 * <p>STORAGE: Fixed price services (drop-off, express, monthly packages)
 * <p>LAUNDRY: Per-weight/per-piece services (wash, dry clean, etc.)
 */
public enum ServiceCategory {
  
  /**
   * Dịch vụ Gửi Đồ - Storage/Drop-off services.
   * Fixed price, payment required before drop-off.
   * Does not require Partner processing.
   */
  STORAGE,
  
  /**
   * Dịch vụ Giặt - Laundry services.
   * Per-weight/per-piece pricing, estimated price shown initially.
   * Final price calculated after Partner weighs items.
   * Payment after items are returned to locker.
   */
  LAUNDRY
}
