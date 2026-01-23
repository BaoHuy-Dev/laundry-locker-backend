package com.huynqb.laundrylockerbackend.module.auth.controller;

import com.huynqb.laundrylockerbackend.core.constant.MessageConstants;
import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
  private final ResponseHelper responseHelper;

  // ===== Phone Authentication Endpoints =====

  /** Phone login endpoint - authenticate with Firebase ID token */
  @Operation(
      summary = "Phone Login",
      description = "Authenticate or register user using Firebase phone verification")
  @PostMapping(UriParamConstants.PHONE_LOGIN)
  public ResponseEntity<ApiResponse<PhoneLoginResponse>> phoneLogin(
      @Valid @RequestBody PhoneLoginRequest request) {

    PhoneLoginResponse response = authService.phoneLogin(request);

    String code = response.isNewUser() ? MessageConstants.AUTH_PHONE_NEW_USER : MessageConstants.AUTH_PHONE_LOGIN_SUCCESS;

    return ResponseEntity.ok(responseHelper.success(response, code));
  }

  /** Complete registration for new phone users */
  @Operation(
      summary = "Complete Registration",
      description = "Complete registration for new phone users with profile info")
  @PostMapping(UriParamConstants.COMPLETE_REGISTRATION)
  public ResponseEntity<ApiResponse<AuthResponse>> completeRegistration(
      @Valid @RequestBody CompleteRegistrationRequest request) {

    AuthResponse authResponse = authService.completeRegistration(request);

    return ResponseEntity.ok(responseHelper.success(authResponse, MessageConstants.AUTH_REGISTRATION_COMPLETE));
  }

  // ===== Email OTP Authentication Endpoints =====

  /** Send OTP to email */
  @Operation(summary = "Send Email OTP", description = "Send OTP code to email for authentication")
  @PostMapping(UriParamConstants.EMAIL_SEND_OTP)
  public ResponseEntity<ApiResponse<Void>> sendEmailOtp(
      @Valid @RequestBody EmailSendOtpRequest request) {

    boolean sent = authService.sendEmailOtp(request);

    if (sent) {
      return ResponseEntity.ok(responseHelper.success(MessageConstants.AUTH_OTP_SENT));
    } else {
      return ResponseEntity.internalServerError().body(responseHelper.error(MessageConstants.AUTH_OTP_SEND_FAILED));
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

    String code = response.isNewUser() ? MessageConstants.AUTH_EMAIL_NEW_USER : MessageConstants.AUTH_EMAIL_LOGIN_SUCCESS;

    return ResponseEntity.ok(responseHelper.success(response, code));
  }

  /** Complete registration for new email users */
  @Operation(
      summary = "Complete Email Registration",
      description = "Complete registration for new email users with profile info")
  @PostMapping(UriParamConstants.EMAIL_COMPLETE_REGISTRATION)
  public ResponseEntity<ApiResponse<AuthResponse>> emailCompleteRegistration(
      @Valid @RequestBody EmailCompleteRegistrationRequest request) {

    AuthResponse authResponse = authService.emailCompleteRegistration(request);

    return ResponseEntity.ok(responseHelper.success(authResponse, MessageConstants.AUTH_REGISTRATION_COMPLETE));
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

    return ResponseEntity.ok(responseHelper.success(authResponse, MessageConstants.AUTH_REFRESH_SUCCESS));
  }

  /** Logout endpoint */
  @Operation(summary = "Logout", description = "Invalidate the current tokens")
  @PostMapping(UriParamConstants.LOGOUT)
  public ResponseEntity<ApiResponse<Void>> logout(
      @Valid @RequestBody LogoutRequest request, HttpServletRequest httpRequest) {

    String accessToken = extractTokenFromRequest(httpRequest);
    authService.logout(accessToken, request);

    return ResponseEntity.ok(responseHelper.success(MessageConstants.AUTH_LOGOUT_SUCCESS));
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
