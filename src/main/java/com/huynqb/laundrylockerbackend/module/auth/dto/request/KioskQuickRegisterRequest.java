package com.huynqb.laundrylockerbackend.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for kiosk quick registration. Only requires tempToken from phone-login or email verify-otp.
 * No personal info required — user can update profile later on mobile app.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KioskQuickRegisterRequest {

  @NotBlank(message = "Temp token is required")
  private String tempToken;
}
