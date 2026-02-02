package com.huynqb.laundrylockerbackend.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for verifying email change with OTP. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Verify email change request - Step 2")
public class VerifyEmailChangeRequest {

  @NotBlank(message = "New email is required")
  @Email(message = "Invalid email format")
  @Schema(description = "New email address", example = "newemail@example.com")
  private String newEmail;

  @NotBlank(message = "OTP is required")
  @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
  @Schema(description = "OTP sent to new email", example = "123456")
  private String otp;
}
