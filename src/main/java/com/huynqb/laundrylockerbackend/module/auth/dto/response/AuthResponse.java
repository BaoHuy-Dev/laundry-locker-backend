package com.huynqb.laundrylockerbackend.module.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for authentication response (login/register/refresh) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

  private String accessToken;
  private String refreshToken;

  @Builder.Default private String tokenType = "Bearer";

  private long expiresIn; // seconds
}
