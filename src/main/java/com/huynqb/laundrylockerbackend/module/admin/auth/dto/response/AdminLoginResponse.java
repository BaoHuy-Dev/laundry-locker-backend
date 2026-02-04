package com.huynqb.laundrylockerbackend.module.admin.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for admin login step 1 (requires 2FA). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Admin login response - requires 2FA verification")
public class AdminLoginResponse {

  @Schema(description = "Whether 2FA is required", example = "true")
  private boolean requiresTwoFactor;

  @Schema(
      description = "Temporary token for 2FA verification",
      example = "admin_2fa_abc123-def456-xyz789")
  private String tempToken;

  @Schema(description = "Token expiration in seconds", example = "600")
  private Long expiresIn;

  @Schema(description = "Masked email for security", example = "ad***@laundrylocker.com")
  private String maskedEmail;

  @Schema(description = "Additional message", example = "OTP has been sent to your email")
  private String message;
}
