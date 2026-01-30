package com.huynqb.laundrylockerbackend.module.admin.auth.controller;

import static com.huynqb.laundrylockerbackend.core.constant.MessageConstants.*;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.auth.dto.request.AdminLoginRequest;
import com.huynqb.laundrylockerbackend.module.admin.auth.dto.request.AdminVerify2faRequest;
import com.huynqb.laundrylockerbackend.module.admin.auth.dto.response.Admin2faResponse;
import com.huynqb.laundrylockerbackend.module.admin.auth.dto.response.AdminLoginResponse;
import com.huynqb.laundrylockerbackend.module.admin.auth.service.AdminAuthService;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.RefreshTokenRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminAuthController - REST API endpoints for admin authentication with 2FA.
 *
 * <p>Authentication Flow:
 *
 * <ol>
 *   <li>POST /login - Submit email + password, receive temp token
 *   <li>POST /verify-2fa - Submit OTP from email, receive JWT tokens
 *   <li>POST /refresh - Refresh access token using refresh token
 * </ol>
 */
@Slf4j
@Tag(name = TagConstants.ROOT_TAG_ADMIN_AUTH)
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_AUTH)
@RequiredArgsConstructor
public class AdminAuthController {

  private final AdminAuthService adminAuthService;
  private final ResponseHelper responseHelper;

  /**
   * Step 1: Admin login with email and password. On success, sends OTP to email and returns temp
   * token.
   */
  @Operation(
      summary = "Admin Login (Step 1)",
      description =
          "Verify admin email and password. On success, sends OTP to email and returns temp token for 2FA verification.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Credentials verified, OTP sent to email",
            content = @Content(schema = @Schema(implementation = AdminLoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials or account locked"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "User is not an admin")
      })
  @PostMapping(UriParamConstants.ADMIN_AUTH_LOGIN)
  public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
      @Valid @RequestBody AdminLoginRequest request) {

    log.info("Admin login attempt for email: {}", request.getEmail());

    AdminLoginResponse response = adminAuthService.login(request);

    return ResponseEntity.ok(responseHelper.success(response, ADMIN_AUTH_2FA_REQUIRED));
  }

  /** Step 2: Verify 2FA OTP code. On success, returns JWT access and refresh tokens. */
  @Operation(
      summary = "Verify 2FA (Step 2)",
      description =
          "Verify OTP code from email. On success, returns JWT access and refresh tokens.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "2FA verified, tokens issued",
            content = @Content(schema = @Schema(implementation = Admin2faResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid or expired OTP/temp token")
      })
  @PostMapping(UriParamConstants.ADMIN_AUTH_VERIFY_2FA)
  public ResponseEntity<ApiResponse<Admin2faResponse>> verify2fa(
      @Valid @RequestBody AdminVerify2faRequest request) {

    log.info("Admin 2FA verification attempt");

    Admin2faResponse response = adminAuthService.verify2fa(request);

    return ResponseEntity.ok(responseHelper.success(response, ADMIN_AUTH_2FA_SUCCESS));
  }

  /** Refresh admin access token. */
  @Operation(
      summary = "Refresh Token",
      description = "Get a new access token using refresh token. Admin role is re-verified.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token")
      })
  @PostMapping(UriParamConstants.ADMIN_AUTH_REFRESH)
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {

    log.info("Admin token refresh attempt");

    AuthResponse response = adminAuthService.refreshToken(request);

    return ResponseEntity.ok(responseHelper.success(response, AUTH_REFRESH_SUCCESS));
  }
}
