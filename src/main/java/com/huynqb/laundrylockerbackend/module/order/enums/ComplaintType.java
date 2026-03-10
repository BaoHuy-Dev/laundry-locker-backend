package com.huynqb.laundrylockerbackend.module.order.enums;

/** Types of order complaints. */
public enum ComplaintType {
  DAMAGED, // Items damaged during processing
  MISSING, // Items missing after processing
  WRONG_ITEM, // Wrong items returned
  QUALITY, // Poor quality of service
  OTHER // Other issues
}
