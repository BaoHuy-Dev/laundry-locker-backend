package com.huynqb.laundrylockerbackend.module.iot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for box unlock operation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockBoxResponse {

  private Boolean success;
  private Long boxId;
  private Integer boxNumber;
  private String lockerCode;
  private Long orderId;
  private String message;

  /** Unlock token for IoT device to verify the unlock command. */
  private String unlockToken;

  /** Timestamp when unlock was authorized. */
  private Long unlockTimestamp;
}
