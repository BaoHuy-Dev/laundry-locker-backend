package com.huynqb.laundrylockerbackend.core.dto;

import com.huynqb.laundrylockerbackend.core.i18n.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Helper for building ApiResponse with i18n message resolution. */
@Component
@RequiredArgsConstructor
public class ResponseHelper {

  private final MessageService messageService;

  /**
   * Build success response with i18n message.
   *
   * @param data Response data
   * @param code Message code from i18n
   * @return ApiResponse with resolved message
   */
  public <T> ApiResponse<T> success(T data, String code) {
    return ApiResponse.<T>builder()
        .success(true)
        .code(code)
        .message(messageService.get(code))
        .data(data)
        .build();
  }

  /**
   * Build success response without data.
   *
   * @param code Message code from i18n
   * @return ApiResponse with resolved message
   */
  public ApiResponse<Void> success(String code) {
    return ApiResponse.<Void>builder()
        .success(true)
        .code(code)
        .message(messageService.get(code))
        .build();
  }

  /**
   * Build error response with i18n message.
   *
   * @param code Error code from i18n
   * @return ApiResponse with resolved message
   */
  public <T> ApiResponse<T> error(String code) {
    return ApiResponse.<T>builder()
        .success(false)
        .code(code)
        .message(messageService.get(code))
        .build();
  }
}
