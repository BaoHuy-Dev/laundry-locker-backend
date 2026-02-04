package com.huynqb.laundrylockerbackend.module.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for enabling/disabling user. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
  @NotNull(message = "Enabled status is required")
  private Boolean enabled;
}
