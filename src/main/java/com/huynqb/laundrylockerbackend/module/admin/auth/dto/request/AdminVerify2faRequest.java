package com.huynqb.laundrylockerbackend.module.admin.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for admin 2FA verification (Step 2: OTP code). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin 2FA verification request with temp token and OTP code")
public class AdminVerify2faRequest {

  @NotBlank(message = "Temp token is required")
  @Schema(
      description = "Temporary token received from login step",
      example = "admin_2fa_abc123-def456-xyz789")
  private String tempToken;

  @NotBlank(message = "OTP code is required")
  @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
  @Schema(description = "6-digit OTP code from email", example = "123456")
  private String otpCode;
}
