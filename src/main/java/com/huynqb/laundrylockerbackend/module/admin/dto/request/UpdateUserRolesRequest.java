package com.huynqb.laundrylockerbackend.module.admin.dto.request;

import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating user roles. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRolesRequest {
  @NotEmpty(message = "Roles cannot be empty")
  private Set<RoleName> roles;
}
