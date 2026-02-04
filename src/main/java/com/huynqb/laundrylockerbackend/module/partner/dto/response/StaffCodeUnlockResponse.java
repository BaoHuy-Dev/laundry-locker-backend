package com.huynqb.laundrylockerbackend.module.partner.dto.response;

import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response after staff unlocks a box using access code. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffCodeUnlockResponse {

  private Boolean success;
  private String message;

  // Order info
  private Long orderId;
  private String orderStatus;

  // Access code info
  private AccessCodeAction action;

  // Box info
  private List<BoxInfo> boxes;
  private String lockerCode;
  private String lockerName;
  private String lockerAddress;

  // Unlock token for IoT device
  private String unlockToken;
  private Long unlockTimestamp;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BoxInfo {
    private Long boxId;
    private String boxNumber;
    private String size;
  }
}
