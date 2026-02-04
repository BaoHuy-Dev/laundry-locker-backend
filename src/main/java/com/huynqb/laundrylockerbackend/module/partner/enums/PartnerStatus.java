package com.huynqb.laundrylockerbackend.module.partner.enums;

/** Partner status enum. */
public enum PartnerStatus {
  /** Initial status when partner registers. */
  PENDING,

  /** Partner has been approved by admin. */
  APPROVED,

  /** Partner application was rejected. */
  REJECTED,

  /** Partner account has been suspended. */
  SUSPENDED
}
