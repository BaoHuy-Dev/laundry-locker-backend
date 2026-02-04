package com.huynqb.laundrylockerbackend.module.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for resetting password with OTP verification. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reset password using OTP")
public class ResetPasswordRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Schema(description = "Email address", example = "user@example.com")
  private String email;

  @NotBlank(message = "OTP is required")
  @Size(min = 6, max = 6, message = "OTP must be 6 digits")
  @Schema(description = "6-digit OTP code sent to email", example = "123456")
  private String otp;

  @NotBlank(message = "New password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
      message =
          "Password must contain at least one uppercase, lowercase, number, and special character")
  @Schema(description = "New password", example = "NewPassword@123")
  private String newPassword;

  @NotBlank(message = "Confirm password is required")
  @Schema(description = "Confirm new password", example = "NewPassword@123")
  private String confirmPassword;
}
