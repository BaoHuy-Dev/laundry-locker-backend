package com.huynqb.laundrylockerbackend.module.laundry.enums;

/** Service type enum for different laundry services. */
public enum ServiceType {
  /** Gửi hàng thường - Standard drop-off */
  STANDARD_DROPOFF,

  /** Qua đêm - Overnight extra charge */
  OVERNIGHT,

  /** Đồ giặt - Laundry items (per kg) */
  LAUNDRY,

  /** Gửi nhanh 2h - Express 2 hours */
  EXPRESS_2H,

  /** Gói tháng sinh viên - Monthly student package */
  MONTHLY_STUDENT,

  /** Gói tháng shipper - Monthly shipper package */
  MONTHLY_SHIPPER,

  /** Phụ phí - Additional fee */
  ADDITIONAL_FEE
}
