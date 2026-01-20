package com.huynqb.laundrylockerbackend.module.auth.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.i18n.MessageService;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.CompleteRegistrationRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.EmailCompleteRegistrationRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.EmailSendOtpRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.EmailVerifyOtpRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.LogoutRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.PhoneLoginRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.RefreshTokenRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.EmailLoginResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.PhoneLoginResponse;
import com.huynqb.laundrylockerbackend.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API endpoints for phone and email OTP authentication. Supports: Phone
 * Login, Email OTP, Complete Registration, Refresh Token, Logout.
 */
@Tag(name = TagConstants.ROOT_TAG_AUTH)
@RequestMapping(UriParamConstants.ROOT_URI_AUTH)
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final MessageService messageService;

  // ===== Phone Authentication Endpoints =====

  /** Phone login endpoint - authenticate with Firebase ID token */
  @Operation(
      summary = "Phone Login",
      description = "Authenticate or register user using Firebase phone verification")
  @PostMapping(UriParamConstants.PHONE_LOGIN)
  public ResponseEntity<ApiResponse<PhoneLoginResponse>> phoneLogin(
      @Valid @RequestBody PhoneLoginRequest request) {

    PhoneLoginResponse response = authService.phoneLogin(request);

    String code = response.isNewUser() ? "AUTH_PHONE_NEW_USER" : "AUTH_PHONE_LOGIN_SUCCESS";
    String message =
        response.isNewUser()
            ? "Số điện thoại chưa đăng ký. Vui lòng hoàn tất thông tin."
            : messageService.get("AUTH_PHONE_LOGIN_SUCCESS");

    return ResponseEntity.ok(
        ApiResponse.<PhoneLoginResponse>builder()
            .success(true)
            .code(code)
            .message(message)
            .data(response)
            .build());
  }

  /** Complete registration for new phone users */
  @Operation(
      summary = "Complete Registration",
      description = "Complete registration for new phone users with profile info")
  @PostMapping(UriParamConstants.COMPLETE_REGISTRATION)
  public ResponseEntity<ApiResponse<AuthResponse>> completeRegistration(
      @Valid @RequestBody CompleteRegistrationRequest request) {

    AuthResponse authResponse = authService.completeRegistration(request);

    return ResponseEntity.ok(
        ApiResponse.<AuthResponse>builder()
            .success(true)
            .code("AUTH_REGISTRATION_COMPLETE")
            .message("Đăng ký thành công!")
            .data(authResponse)
            .build());
  }

  // ===== Email OTP Authentication Endpoints =====

  /** Send OTP to email */
  @Operation(summary = "Send Email OTP", description = "Send OTP code to email for authentication")
  @PostMapping(UriParamConstants.EMAIL_SEND_OTP)
  public ResponseEntity<ApiResponse<Void>> sendEmailOtp(
      @Valid @RequestBody EmailSendOtpRequest request) {

    boolean sent = authService.sendEmailOtp(request);

    if (sent) {
      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .code("AUTH_OTP_SENT")
              .message("Mã OTP đã được gửi đến email của bạn.")
              .build());
    } else {
      return ResponseEntity.internalServerError()
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .code("AUTH_OTP_SEND_FAILED")
                  .message("Không thể gửi OTP. Vui lòng thử lại sau.")
                  .build());
    }
  }

  /** Verify email OTP and login */
  @Operation(
      summary = "Verify Email OTP",
      description = "Verify OTP and login/register user via email")
  @PostMapping(UriParamConstants.EMAIL_VERIFY_OTP)
  public ResponseEntity<ApiResponse<EmailLoginResponse>> verifyEmailOtp(
      @Valid @RequestBody EmailVerifyOtpRequest request) {

    EmailLoginResponse response = authService.verifyEmailOtp(request);

    String code = response.isNewUser() ? "AUTH_EMAIL_NEW_USER" : "AUTH_EMAIL_LOGIN_SUCCESS";
    String message =
        response.isNewUser()
            ? "Email chưa đăng ký. Vui lòng hoàn tất thông tin."
            : "Đăng nhập thành công!";

    return ResponseEntity.ok(
        ApiResponse.<EmailLoginResponse>builder()
            .success(true)
            .code(code)
            .message(message)
            .data(response)
            .build());
  }

  /** Complete registration for new email users */
  @Operation(
      summary = "Complete Email Registration",
      description = "Complete registration for new email users with profile info")
  @PostMapping(UriParamConstants.EMAIL_COMPLETE_REGISTRATION)
  public ResponseEntity<ApiResponse<AuthResponse>> emailCompleteRegistration(
      @Valid @RequestBody EmailCompleteRegistrationRequest request) {

    AuthResponse authResponse = authService.emailCompleteRegistration(request);

    return ResponseEntity.ok(
        ApiResponse.<AuthResponse>builder()
            .success(true)
            .code("AUTH_REGISTRATION_COMPLETE")
            .message("Đăng ký thành công!")
            .data(authResponse)
            .build());
  }

  // ===== Token Management Endpoints =====

  /** Refresh access token endpoint */
  @Operation(
      summary = "Refresh token",
      description = "Generate a new access token using a refresh token")
  @PostMapping(UriParamConstants.REFRESH_TOKEN)
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    AuthResponse authResponse = authService.refreshToken(request);

    return ResponseEntity.ok(
        ApiResponse.<AuthResponse>builder()
            .success(true)
            .code("AUTH_REFRESH_SUCCESS")
            .message(messageService.get("AUTH_REFRESH_SUCCESS"))
            .data(authResponse)
            .build());
  }

  /** Logout endpoint */
  @Operation(summary = "Logout", description = "Invalidate the current tokens")
  @PostMapping(UriParamConstants.LOGOUT)
  public ResponseEntity<ApiResponse<Void>> logout(
      @Valid @RequestBody LogoutRequest request, HttpServletRequest httpRequest) {

    String accessToken = extractTokenFromRequest(httpRequest);
    authService.logout(accessToken, request);

    return ResponseEntity.ok(
        ApiResponse.<Void>builder()
            .success(true)
            .code("AUTH_LOGOUT_SUCCESS")
            .message(messageService.get("AUTH_LOGOUT_SUCCESS"))
            .build());
  }

  // ===== Helper Methods =====

  /** Extract JWT token from Authorization header */
  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
