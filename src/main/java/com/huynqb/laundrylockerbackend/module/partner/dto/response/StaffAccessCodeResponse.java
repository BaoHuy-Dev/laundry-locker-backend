package com.huynqb.laundrylockerbackend.module.partner.dto.response;

import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response containing staff access code information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAccessCodeResponse {

  private Long id;
  private String code;
  private Long orderId;
  private Long partnerId;
  private AccessCodeAction action;
  private AccessCodeStatus status;
  private LocalDateTime expiresAt;
  private LocalDateTime usedAt;
  private String staffName;
  private String notes;
  private LocalDateTime createdAt;

  // Order info
  private String orderLockerCode;
  private String orderLockerName;
  private String orderBoxNumbers;
  private String customerName;
}
