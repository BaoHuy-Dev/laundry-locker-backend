package com.huynqb.laundrylockerbackend.module.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for admin to create a new user (e.g., Staff account). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
  private String password;

  @NotBlank(message = "First name is required")
  @Size(max = 100, message = "First name must be at most 100 characters")
  private String firstName;

  @Size(max = 100, message = "Last name must be at most 100 characters")
  private String lastName;

  @Size(max = 20, message = "Phone number must be at most 20 characters")
  private String phoneNumber;

  /** Roles to assign to the user. E.g., ["USER", "STAFF"] */
  private Set<String> roles;

  /** Whether the user is enabled. Default is true. */
  @Builder.Default private Boolean enabled = true;
}
