package com.huynqb.laundrylockerbackend.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for completing phone registration with user profile info. Used after phone OTP verification
 * for new users.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteRegistrationRequest {

  @NotBlank(message = "Firebase ID token is required")
  private String idToken;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  @NotNull(message = "Birthday is required")
  private LocalDate birthday;
}
