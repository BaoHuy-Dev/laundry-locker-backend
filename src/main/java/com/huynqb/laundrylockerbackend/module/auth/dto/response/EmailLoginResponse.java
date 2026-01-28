package com.huynqb.laundrylockerbackend.module.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response for email OTP verification. Contains isNewUser flag to determine if client should show
 * registration form or proceed with login.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLoginResponse {

  private String accessToken;
  private String refreshToken;

  @Builder.Default private String tokenType = "Bearer";

  private Long expiresIn;

  /** True if this is a new user who needs to complete registration. */
  private boolean isNewUser;

  /** True if OTP was verified successfully. */
  private boolean otpVerified;

  /** Temporary token for registration completion (only for new users). */
  private String tempToken;
}
