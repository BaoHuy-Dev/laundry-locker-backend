package com.huynqb.laundrylockerbackend.module.iot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating box status from IoT device/sensor. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxStatusUpdateRequest {

  @NotNull(message = "Box ID is required")
  private Long boxId;

  @NotBlank(message = "Status is required")
  private String status;

  /** Device identifier for authentication. */
  private String deviceId;

  /** Whether the box door is currently open. */
  private Boolean isDoorOpen;
}
