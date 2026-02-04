package com.huynqb.laundrylockerbackend.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for changing phone number - Step 1: Request OTP. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change phone number request - Step 1")
public class ChangePhoneRequest {

  @NotBlank(message = "New phone number is required")
  @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
  @Schema(description = "New phone number", example = "+84901234567")
  private String newPhone;

  @NotBlank(message = "Password is required")
  @Schema(description = "Current password for verification")
  private String password;
}
