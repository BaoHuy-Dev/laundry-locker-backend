package com.huynqb.laundrylockerbackend.core.exception;

import org.springframework.http.HttpStatus;

/** Exception thrown when rate limit is exceeded. Returns HTTP 429 Too Many Requests. */
public class RateLimitExceededException extends BaseException {

  public RateLimitExceededException(String code) {
    super(code, HttpStatus.TOO_MANY_REQUESTS);
  }

  public RateLimitExceededException(String code, String message) {
    super(code, HttpStatus.TOO_MANY_REQUESTS, message);
  }

  public RateLimitExceededException(String code, String message, Object... args) {
    super(code, HttpStatus.TOO_MANY_REQUESTS, message, args);
  }
}
