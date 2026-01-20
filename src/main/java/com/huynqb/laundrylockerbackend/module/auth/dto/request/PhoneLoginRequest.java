package com.huynqb.laundrylockerbackend.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for phone login request. Contains Firebase ID token from client-side authentication. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneLoginRequest {

  @NotBlank(message = "Firebase ID token is required")
  private String idToken;
}
