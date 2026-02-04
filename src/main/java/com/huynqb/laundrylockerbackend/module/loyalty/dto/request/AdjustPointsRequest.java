package com.huynqb.laundrylockerbackend.module.loyalty.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for admin to adjust points. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustPointsRequest {

  @NotNull(message = "User ID is required")
  private Long userId;

  @NotNull(message = "Points to adjust is required")
  private Long points;

  private String reason;
}
