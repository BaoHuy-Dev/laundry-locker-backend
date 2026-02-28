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
 * DTO for completing email registration. Used when a new user verifies OTP and needs to provide
 * profile information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailCompleteRegistrationRequest {

  @NotBlank(message = "Temp token is required")
  private String tempToken;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  @NotNull(message = "Birthday is required")
  private LocalDate birthday;

  private String phoneNumber;
}
