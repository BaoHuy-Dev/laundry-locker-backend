package com.huynqb.laundrylockerbackend.module.user.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Admin operations. */
@Tag(name = TagConstants.ROOT_TAG_ADMIN)
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN)
@RestController
public class AdminController {

  @Operation(summary = "Hello Admin", description = "Admin greeting endpoint")
  @GetMapping(UriParamConstants.HELLO)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> helloAdmin() {
    return ResponseEntity.ok(
        ApiResponse.<String>builder().success(true).data("Hello, admin!").build());
  }
}
