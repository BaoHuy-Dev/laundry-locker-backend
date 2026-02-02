package com.huynqb.laundrylockerbackend.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for verifying phone change with OTP. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Verify phone change request - Step 2")
public class VerifyPhoneChangeRequest {

  @NotBlank(message = "New phone number is required")
  @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
  @Schema(description = "New phone number", example = "+84901234567")
  private String newPhone;

  @NotBlank(message = "OTP is required")
  @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
  @Schema(description = "OTP sent to new phone number", example = "123456")
  private String otp;
}
