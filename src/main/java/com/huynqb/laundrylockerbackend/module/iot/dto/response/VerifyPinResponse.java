package com.huynqb.laundrylockerbackend.module.iot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for PIN verification result. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPinResponse {

  private Boolean valid;
  private Long orderId;
  private Long boxId;
  private Integer boxNumber;
  private String lockerCode;
  private String orderStatus;
  private String message;
}
