package com.huynqb.laundrylockerbackend.module.partner.dto.response;

import com.huynqb.laundrylockerbackend.module.partner.enums.PartnerStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for partner information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerResponse {

  private Long id;
  private Long userId;
  private String userName;
  private String businessName;
  private String businessRegistrationNumber;
  private String taxId;
  private String businessAddress;
  private String contactPhone;
  private String contactEmail;
  private PartnerStatus status;
  private LocalDateTime approvedAt;
  private Long approvedBy;
  private String rejectionReason;
  private BigDecimal revenueSharePercent;
  private int storeCount;
  private int staffCount;
  private String notes;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
