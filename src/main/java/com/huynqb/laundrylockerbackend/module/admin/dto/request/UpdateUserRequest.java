package com.huynqb.laundrylockerbackend.module.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating user information by admin. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  private String name;

  @Email(message = "Invalid email format")
  private String email;

  private String imageUrl;
}
