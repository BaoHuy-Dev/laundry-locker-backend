package com.huynqb.laundrylockerbackend.module.order.exception;

/** Exception thrown when order-related operations fail. */
public class OrderException extends RuntimeException {

  private final String code;

  public OrderException(String code) {
    super(code);
    this.code = code;
  }

  public OrderException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
