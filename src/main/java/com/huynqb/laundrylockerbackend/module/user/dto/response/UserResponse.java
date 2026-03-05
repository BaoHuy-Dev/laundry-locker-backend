package com.huynqb.laundrylockerbackend.module.user.dto.response;

import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for user profile response */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

  private Long id;
  private String email;
  private String name;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String imageUrl;
  private AuthProvider provider;
  private Boolean emailVerified;
  private Boolean phoneVerified;
  private LocalDateTime joinDate;

  /** User roles (e.g., USER, ADMIN, PARTNER) */
  private Set<String> roles;
}
