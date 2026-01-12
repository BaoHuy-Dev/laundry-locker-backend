package com.huynqb.laundrylockerbackend.module.auth.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.i18n.MessageService;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.ForgotPasswordRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.LoginRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.LogoutRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.RefreshTokenRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.RegisterRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.ResendVerificationRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.ResetPasswordRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.service.AuthService;
import com.huynqb.laundrylockerbackend.module.auth.service.EmailVerificationService;
import com.huynqb.laundrylockerbackend.module.auth.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API endpoints for authentication Includes: Login, Register, Refresh Token,
 * Logout, Email Verification, Password Reset
 */
@Tag(name = TagConstants.ROOT_TAG_AUTH)
@RequestMapping(UriParamConstants.ROOT_URI_AUTH)
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final MessageService messageService;
  private final EmailVerificationService emailVerificationService;
  private final PasswordResetService passwordResetService;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  // ===== Authentication Endpoints =====

  /** Login endpoint */
  @Operation(summary = "Login", description = "Authenticate the user and return JWT tokens")
  @PostMapping(UriParamConstants.LOGIN)
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse authResponse = authService.login(request);

    return ResponseEntity.ok(
        ApiResponse.<AuthResponse>builder()
            .success(true)
            .code("AUTH_LOGIN_SUCCESS")
            .message(messageService.get("AUTH_LOGIN_SUCCESS"))
            .data(authResponse)
            .build());
  }

  /** Register new user endpoint */
  @Operation(
      summary = "Register",
      description = "Create a new account and send a verification email")
  @PostMapping(UriParamConstants.REGISTER)
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    AuthResponse authResponse = authService.register(request);

    return ResponseEntity.ok(
        ApiResponse.<AuthResponse>builder()
            .success(true)
            .code("AUTH_REGISTER_SUCCESS")
            .message(messageService.get("AUTH_REGISTER_SUCCESS"))
            .data(authResponse)
            .build());
  }

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

  // ===== Email Verification Endpoints =====

  /** Verify email endpoint (called from email link) */
  @Operation(
      summary = "Email verification",
      description = "Verify the user's email address using the link sent via email")
  @GetMapping(UriParamConstants.VERIFY_EMAIL)
  public ResponseEntity<String> verifyEmail(@RequestParam String token) {
    boolean verified = emailVerificationService.verifyEmail(token);

    if (verified) {
      // Redirect to frontend success page
      return ResponseEntity.status(302)
          .header("Location", frontendUrl + "/email-verified?success=true")
          .build();
    } else {
      // Redirect to frontend error page
      return ResponseEntity.status(302)
          .header("Location", frontendUrl + "/email-verified?success=false&error=invalid_token")
          .build();
    }
  }

  /** Resend verification email endpoint */
  @Operation(
      summary = "Resend verification email",
      description = "Resend the verification email to the user")
  @PostMapping(UriParamConstants.RESEND_VERIFICATION)
  public ResponseEntity<ApiResponse<Void>> resendVerification(
      @Valid @RequestBody ResendVerificationRequest request) {

    boolean sent = emailVerificationService.resendVerificationEmail(request.getEmail());

    // Always return success to prevent email enumeration
    return ResponseEntity.ok(
        ApiResponse.<Void>builder()
            .success(true)
            .code("AUTH_VERIFICATION_SENT")
            .message(messageService.get("AUTH_VERIFICATION_SENT"))
            .build());
  }

  // ===== Password Reset Endpoints =====

  /** Forgot password endpoint - initiate password reset */
  @Operation(summary = "Forgot password", description = "Send a password reset email")
  @PostMapping(UriParamConstants.FORGET_PASSWORD)
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {

    passwordResetService.initiatePasswordReset(request.getEmail());

    // Always return success to prevent email enumeration
    return ResponseEntity.ok(
        ApiResponse.<Void>builder()
            .success(true)
            .code("AUTH_RESET_EMAIL_SENT")
            .message(messageService.get("AUTH_RESET_EMAIL_SENT"))
            .build());
  }

  /** Reset password endpoint - set new password */
  @Operation(summary = "Reset password", description = "Set a new password using a reset token")
  @PostMapping(UriParamConstants.RESET_PASSWORD)
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {

    // Validate password confirmation
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .code("E_VALIDATION002")
                  .message(messageService.get("E_VALIDATION002"))
                  .build());
    }

    passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

    return ResponseEntity.ok(
        ApiResponse.<Void>builder()
            .success(true)
            .code("AUTH_PASSWORD_RESET_SUCCESS")
            .message(messageService.get("AUTH_PASSWORD_RESET_SUCCESS"))
            .build());
  }

  /** Validate reset token endpoint (for frontend to check before showing form) */
  @Operation(
      summary = "Validate reset token",
      description = "Check whether the password reset token is valid")
  @GetMapping(UriParamConstants.VALIDATE_RESET_TOKEN)
  public ResponseEntity<ApiResponse<Boolean>> validateResetToken(@RequestParam String token) {
    boolean valid = passwordResetService.validateResetToken(token);

    return ResponseEntity.ok(
        ApiResponse.<Boolean>builder()
            .success(true)
            .code(valid ? "TOKEN_VALID" : "TOKEN_INVALID")
            .message(
                valid ? messageService.get("TOKEN_VALID") : messageService.get("TOKEN_INVALID"))
            .data(valid)
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
