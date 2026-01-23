package com.huynqb.laundrylockerbackend.module.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating user profile. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

  @Size(max = 100, message = "First name must be at most 100 characters")
  private String firstName;

  @Size(max = 100, message = "Last name must be at most 100 characters")
  private String lastName;

  @Email(message = "Invalid email format")
  private String email;

  @Size(max = 20, message = "Phone number must be at most 20 characters")
  private String phoneNumber;

  private LocalDate birthday;

  @Size(max = 1000, message = "Image URL must be at most 1000 characters")
  private String imageUrl;
}
