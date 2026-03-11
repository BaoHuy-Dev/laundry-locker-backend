package com.huynqb.laundrylockerbackend.module.locker.dto.response;

import com.huynqb.laundrylockerbackend.module.locker.enums.LockerReportStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.LockerReport;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LockerReportResponse {
  private Long id;
  private Long lockerId;
  private String lockerName;
  private Long userId;
  private String userFullName;
  private String userEmail;
  private String userPhone;
  private String description;
  private LockerReportStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime resolvedAt;

  public static LockerReportResponse from(LockerReport report) {
    User user = report.getUser();
    String fullName =
        user.getFirstName() != null && user.getLastName() != null
            ? user.getLastName() + " " + user.getFirstName()
            : user.getName();

    return LockerReportResponse.builder()
        .id(report.getId())
        .lockerId(report.getLocker().getId())
        .lockerName(report.getLocker().getName())
        .userId(user.getId())
        .userFullName(fullName)
        .userEmail(user.getEmail())
        .userPhone(user.getPhoneNumber())
        .description(report.getDescription())
        .status(report.getStatus())
        .createdAt(report.getCreatedAt())
        .resolvedAt(report.getResolvedAt())
        .build();
  }
}
