package com.huynqb.laundrylockerbackend.core.exception;

import org.springframework.http.HttpStatus;

/** Exception thrown when an external service call fails. Returns HTTP 502 Bad Gateway. */
public class ExternalServiceException extends BaseException {

  private final String serviceName;

  public ExternalServiceException(String code, String serviceName) {
    super(code, HttpStatus.BAD_GATEWAY);
    this.serviceName = serviceName;
  }

  public ExternalServiceException(String code, String serviceName, String message) {
    super(code, HttpStatus.BAD_GATEWAY, message);
    this.serviceName = serviceName;
  }

  public ExternalServiceException(
      String code, String serviceName, String message, Throwable cause) {
    super(code, HttpStatus.BAD_GATEWAY, message, cause);
    this.serviceName = serviceName;
  }

  public String getServiceName() {
    return serviceName;
  }
}
