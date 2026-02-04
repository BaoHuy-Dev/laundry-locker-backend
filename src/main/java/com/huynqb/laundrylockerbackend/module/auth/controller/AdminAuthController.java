package com.huynqb.laundrylockerbackend.module.auth.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.AdminLoginRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AdminAuthController - REST API endpoints for admin authentication. Provides password-based login
 * for admin users.
 */
@Tag(name = TagConstants.ROOT_TAG_AUTH, description = "Admin Authentication APIs")
@RequestMapping("/api/admin/auth")
@RestController
@RequiredArgsConstructor
public class AdminAuthController {

  private final AdminAuthService adminAuthService;
  private final ResponseHelper responseHelper;

  /** Admin login endpoint - authenticate admin with email and password */
  @Operation(
      summary = "Admin Login",
      description = "Authenticate admin user with email and password")
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> adminLogin(
      @Valid @RequestBody AdminLoginRequest request) {

    AuthResponse response = adminAuthService.adminLogin(request);

    return ResponseEntity.ok(responseHelper.success(response, "AUTH_ADMIN_LOGIN_SUCCESS"));
  }
}
