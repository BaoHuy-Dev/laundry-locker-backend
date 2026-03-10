package com.huynqb.laundrylockerbackend.module.order.enums;

/** Status of order complaints. */
public enum ComplaintStatus {
  PENDING, // Complaint submitted, awaiting review
  INVESTIGATING, // Under investigation by admin/partner
  RESOLVED, // Complaint resolved
  REJECTED // Complaint rejected
}
