package com.huynqb.laundrylockerbackend.module.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Response for phone login. Extends AuthResponse with isNewUser flag. If isNewUser is true, client
 * should show registration form.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PhoneLoginResponse {

  private String accessToken;
  private String refreshToken;

  @Builder.Default private String tokenType = "Bearer";

  private Long expiresIn;

  /** True if this is a new user who needs to complete registration */
  private boolean isNewUser;
}
