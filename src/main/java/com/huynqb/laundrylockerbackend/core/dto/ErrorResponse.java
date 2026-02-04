package com.huynqb.laundrylockerbackend.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Standardized error response for API errors. Follows RFC 7807 Problem Details for HTTP APIs
 * pattern.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorResponse {

  /** Timestamp when the error occurred */
  Instant timestamp;

  /** HTTP status code */
  Integer status;

  /** Error code for client identification */
  String code;

  /** Human-readable error message */
  String message;

  /** Request path that caused the error */
  String path;

  /** Unique trace ID for error tracking */
  String traceId;

  /** List of detailed field-level errors for validation failures */
  List<FieldErrorDetail> errors;

  /** Additional debug information (only in non-production) */
  String debugMessage;

  /** Create a simple error response. */
  public static ErrorResponse of(
      int status, String code, String message, String path, String traceId) {
    return ErrorResponse.builder()
        .timestamp(Instant.now())
        .status(status)
        .code(code)
        .message(message)
        .path(path)
        .traceId(traceId)
        .build();
  }

  /** Create an error response with field errors. */
  public static ErrorResponse withFieldErrors(
      int status,
      String code,
      String message,
      String path,
      String traceId,
      List<FieldErrorDetail> errors) {
    return ErrorResponse.builder()
        .timestamp(Instant.now())
        .status(status)
        .code(code)
        .message(message)
        .path(path)
        .traceId(traceId)
        .errors(errors)
        .build();
  }

  /** Field-level error detail for validation errors. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class FieldErrorDetail {

    /** The field name that has validation error */
    String field;

    /** Error code for this field */
    String code;

    /** Human-readable error message */
    String message;

    /** The value that was rejected */
    Object rejectedValue;
  }
}
