package com.huynqb.laundrylockerbackend.module.locker.dto.response;

import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for Box. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxResponse {

  private Long id;
  private Integer boxNumber;
  private Boolean isActive;
  private BoxStatus status;
  private String description;
  private Long lockerId;
  private String lockerCode;
}
