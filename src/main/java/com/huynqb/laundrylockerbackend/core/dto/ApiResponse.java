package com.huynqb.laundrylockerbackend.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {

  boolean success;
  String code;
  String message;

  @Builder.Default List<Object> params = Collections.emptyList();
  T data;
  @Builder.Default List<Error> errors = Collections.emptyList();

  /**
   * Create a success response with data and message code.
   *
   * @param data Response data
   * @param messageCode Message code for i18n
   * @return ApiResponse with success=true
   */
  public static <T> ApiResponse<T> success(T data, String messageCode) {
    return ApiResponse.<T>builder().success(true).code(messageCode).data(data).build();
  }

  /**
   * Create a success response with data only.
   *
   * @param data Response data
   * @return ApiResponse with success=true
   */
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder().success(true).data(data).build();
  }

  /**
   * Create an error response.
   *
   * @param code Error code
   * @param message Error message
   * @return ApiResponse with success=false
   */
  public static <T> ApiResponse<T> error(String code, String message) {
    return ApiResponse.<T>builder().success(false).code(code).message(message).build();
  }

  /**
   * Create an error response with errors list.
   *
   * @param code Error code
   * @param message Error message
   * @param errors List of errors
   * @return ApiResponse with success=false
   */
  public static <T> ApiResponse<T> error(String code, String message, List<Error> errors) {
    return ApiResponse.<T>builder()
        .success(false)
        .code(code)
        .message(message)
        .errors(errors)
        .build();
  }
}
