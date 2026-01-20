package com.huynqb.laundrylockerbackend.module.auth.exception;

import lombok.Getter;

/**
 * Custom exception for Firebase authentication errors. Contains error code for i18n message lookup.
 */
@Getter
public class FirebaseAuthException extends RuntimeException {

  private final String code;

  public FirebaseAuthException(String code, String message) {
    super(message);
    this.code = code;
  }

  public FirebaseAuthException(String code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }
}
