package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for admin user operations. Contains full user details including roles. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
  private Long id;
  private String email;
  private String name;
  private String imageUrl;
  private AuthProvider provider;
  private Boolean emailVerified;
  private Boolean enabled;
  private Set<String> roles;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
