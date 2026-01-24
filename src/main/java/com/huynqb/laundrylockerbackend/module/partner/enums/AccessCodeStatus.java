package com.huynqb.laundrylockerbackend.module.partner.enums;

/** Status of a staff access code. */
public enum AccessCodeStatus {
  ACTIVE, // Code is active and can be used
  USED, // Code has been used
  EXPIRED, // Code has expired
  CANCELLED // Code was cancelled by partner
}
