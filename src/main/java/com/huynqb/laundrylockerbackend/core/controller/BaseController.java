package com.huynqb.laundrylockerbackend.core.controller;

import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.i18n.MessageService;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;

public abstract class BaseController {

  protected final MessageService messageService;

  protected BaseController(MessageService messageService) {
    this.messageService = messageService;
  }

  protected String i18n(String code, Object... args) {
    return messageService.get(code, args);
  }

  /** Build response with data envelope (without ResponseEntity). */
  protected <T> ApiResponse<T> response(
      HttpStatus status, T data, String code, String message, List<Object> params) {

    boolean success = status.is2xxSuccessful();

    return ApiResponse.<T>builder()
        .success(success)
        .code(code)
        .message(message)
        .params(params != null ? params : Collections.emptyList())
        .data(data)
        .errors(Collections.emptyList())
        .build();
  }
}
