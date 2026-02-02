package com.huynqb.laundrylockerbackend.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for changing email - Step 1: Request OTP. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change email request - Step 1")
public class ChangeEmailRequest {

  @NotBlank(message = "New email is required")
  @Email(message = "Invalid email format")
  @Schema(description = "New email address", example = "newemail@example.com")
  private String newEmail;

  @NotBlank(message = "Password is required")
  @Schema(description = "Current password for verification")
  private String password;
}
