package com.huynqb.laundrylockerbackend.module.loyalty.exception;

import com.huynqb.laundrylockerbackend.core.exception.BusinessException;

public class LoyaltyException extends BusinessException {

  public LoyaltyException(String errorCode) {
    super(errorCode);
  }

  public LoyaltyException(String errorCode, String message) {
    super(errorCode, message);
  }

  public LoyaltyException(String errorCode, String message, Throwable cause) {
    super(errorCode, message, cause);
  }
}
