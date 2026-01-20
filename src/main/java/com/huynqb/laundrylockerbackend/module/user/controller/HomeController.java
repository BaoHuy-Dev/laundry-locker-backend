package com.huynqb.laundrylockerbackend.module.user.controller;

import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for home/public endpoints. */
@RestController
public class HomeController {

  @Operation(summary = "Home", description = "Public home endpoint")
  @GetMapping("/")
  public ResponseEntity<ApiResponse<String>> home() {
    return ResponseEntity.ok(
        ApiResponse.<String>builder().success(true).data("Welcome to the Home Page!").build());
  }

  @Operation(summary = "Secured", description = "Secured endpoint for authenticated users")
  @GetMapping(UriParamConstants.SECURED)
  public ResponseEntity<ApiResponse<String>> secured() {
    return ResponseEntity.ok(
        ApiResponse.<String>builder().success(true).data("Welcome to the Secured Page!").build());
  }
}
