package com.huynqb.laundrylockerbackend.module.staff.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for staff unlock box operation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffUnlockBoxResponse {

  private Boolean success;
  private Long boxId;
  private Integer boxNumber;
  private String lockerCode;
  private String lockerName;
  private Long orderId;
  private String message;

  /** Unlock token for IoT device verification. */
  private String unlockToken;

  /** Timestamp when unlock was authorized. */
  private Long unlockTimestamp;
}
