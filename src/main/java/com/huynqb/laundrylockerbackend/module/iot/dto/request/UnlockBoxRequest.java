package com.huynqb.laundrylockerbackend.module.iot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for unlocking a box. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockBoxRequest {

  @NotNull(message = "Box ID is required")
  private Long boxId;

  @NotBlank(message = "PIN code is required")
  @Pattern(regexp = "^\\d{6}$", message = "PIN code must be 6 digits")
  private String pinCode;

  /** Action type: DROP_OFF (customer dropping items) or PICKUP (customer picking up items). */
  private String actionType;
}
