package com.huynqb.laundrylockerbackend.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all custom business exceptions. Provides common functionality for error
 * code and HTTP status handling.
 */
@Getter
public abstract class BaseException extends RuntimeException {

  private final String code;
  private final HttpStatus httpStatus;
  private final Object[] args;

  protected BaseException(String code, HttpStatus httpStatus) {
    super(code);
    this.code = code;
    this.httpStatus = httpStatus;
    this.args = new Object[0];
  }

  protected BaseException(String code, HttpStatus httpStatus, String message) {
    super(message);
    this.code = code;
    this.httpStatus = httpStatus;
    this.args = new Object[0];
  }

  protected BaseException(String code, HttpStatus httpStatus, String message, Object... args) {
    super(message);
    this.code = code;
    this.httpStatus = httpStatus;
    this.args = args;
  }

  protected BaseException(String code, HttpStatus httpStatus, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.httpStatus = httpStatus;
    this.args = new Object[0];
  }

  protected BaseException(String code, HttpStatus httpStatus, Throwable cause, Object... args) {
    super(code, cause);
    this.code = code;
    this.httpStatus = httpStatus;
    this.args = args;
  }
}
