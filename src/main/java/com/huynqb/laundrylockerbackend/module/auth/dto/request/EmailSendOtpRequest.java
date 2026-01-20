package com.huynqb.laundrylockerbackend.module.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for requesting OTP to be sent to email. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailSendOtpRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;
}
