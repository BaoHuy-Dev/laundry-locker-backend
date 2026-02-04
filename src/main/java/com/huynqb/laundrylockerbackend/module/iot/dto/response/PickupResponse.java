package com.huynqb.laundrylockerbackend.module.iot.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for customer pickup confirmation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickupResponse {

  private Boolean success;
  private Long orderId;
  private String orderStatus;
  private LocalDateTime completedAt;
  private String message;
}
