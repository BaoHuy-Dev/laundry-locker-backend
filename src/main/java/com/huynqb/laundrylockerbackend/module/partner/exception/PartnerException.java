package com.huynqb.laundrylockerbackend.module.partner.exception;

import com.huynqb.laundrylockerbackend.core.exception.BusinessException;

/** Exception for partner-related errors. */
public class PartnerException extends BusinessException {

  public PartnerException(String code, String message) {
    super(code, message);
  }

  public static PartnerException notFound() {
    return new PartnerException("E_PARTNER001", "Partner not found");
  }

  public static PartnerException alreadyExists() {
    return new PartnerException("E_PARTNER002", "User already has a partner account");
  }

  public static PartnerException notApproved() {
    return new PartnerException("E_PARTNER003", "Partner account is not approved");
  }

  public static PartnerException suspended() {
    return new PartnerException("E_PARTNER004", "Partner account is suspended");
  }

  public static PartnerException cannotModify() {
    return new PartnerException("E_PARTNER005", "Cannot modify partner in current status");
  }

  public static PartnerException storeNotOwned() {
    return new PartnerException("E_PARTNER006", "Store does not belong to this partner");
  }

  public static PartnerException staffNotFound() {
    return new PartnerException("E_PARTNER007", "Staff not found");
  }
}
