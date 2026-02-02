package com.huynqb.laundrylockerbackend.module.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for forgot password - sends OTP to email. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to initiate forgot password flow")
public class ForgotPasswordRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Schema(description = "Email address to send reset OTP", example = "user@example.com")
  private String email;
}
