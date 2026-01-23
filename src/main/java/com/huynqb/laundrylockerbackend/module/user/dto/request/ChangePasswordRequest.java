package com.huynqb.laundrylockerbackend.module.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for changing password. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

  @NotBlank(message = "Current password is required")
  private String currentPassword;

  @NotBlank(message = "New password is required")
  @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
  private String newPassword;

  @NotBlank(message = "Confirm password is required")
  private String confirmPassword;
}
