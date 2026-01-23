package com.huynqb.laundrylockerbackend.core.exception;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Exception thrown when request validation fails. Contains detailed field-level errors. */
@Getter
public class ValidationException extends BaseException {

  private final List<FieldError> fieldErrors;

  public ValidationException(String code) {
    super(code, HttpStatus.BAD_REQUEST);
    this.fieldErrors = new ArrayList<>();
  }

  public ValidationException(String code, String message) {
    super(code, HttpStatus.BAD_REQUEST, message);
    this.fieldErrors = new ArrayList<>();
  }

  public ValidationException(String code, List<FieldError> fieldErrors) {
    super(code, HttpStatus.BAD_REQUEST);
    this.fieldErrors = fieldErrors != null ? fieldErrors : new ArrayList<>();
  }

  public ValidationException(String code, String message, List<FieldError> fieldErrors) {
    super(code, HttpStatus.BAD_REQUEST, message);
    this.fieldErrors = fieldErrors != null ? fieldErrors : new ArrayList<>();
  }

  public ValidationException addFieldError(String field, String code, String message) {
    this.fieldErrors.add(new FieldError(field, code, message));
    return this;
  }

  public boolean hasErrors() {
    return !fieldErrors.isEmpty();
  }

  /** Represents a single field validation error. */
  @Getter
  public static class FieldError {
    private final String field;
    private final String code;
    private final String message;
    private final Object rejectedValue;

    public FieldError(String field, String code, String message) {
      this.field = field;
      this.code = code;
      this.message = message;
      this.rejectedValue = null;
    }

    public FieldError(String field, String code, String message, Object rejectedValue) {
      this.field = field;
      this.code = code;
      this.message = message;
      this.rejectedValue = rejectedValue;
    }
  }
}
