package com.huynqb.laundrylockerbackend.module.auth.dto.response;

import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import java.util.Set;
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

  /** Phone number for new user registration flow */
  private String phoneNumber;

  /** Temporary token for completing registration (valid for 10 minutes) */
  private String tempToken;

  /** User information (only for existing users) */
  private UserResponse userInfo;

  /** User roles (e.g., USER, ADMIN, PARTNER) - only for existing users */
  private Set<String> roles;
}
