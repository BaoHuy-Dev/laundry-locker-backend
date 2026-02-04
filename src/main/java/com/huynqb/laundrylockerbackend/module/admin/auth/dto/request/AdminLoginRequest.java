package com.huynqb.laundrylockerbackend.module.admin.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for admin login (Step 1: Email + Password). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin login request with email and password")
public class AdminLoginRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Schema(description = "Admin email address", example = "admin@laundrylocker.com")
  private String email;

  @NotBlank(message = "Password is required")
  @Schema(description = "Admin password", example = "Admin@123456")
  private String password;
}
