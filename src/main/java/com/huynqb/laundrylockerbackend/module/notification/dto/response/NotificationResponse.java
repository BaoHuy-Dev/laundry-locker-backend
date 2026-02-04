package com.huynqb.laundrylockerbackend.module.notification.dto.response;

import com.huynqb.laundrylockerbackend.module.notification.enums.NotificationStatus;
import com.huynqb.laundrylockerbackend.module.notification.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Response DTO for notification. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

  private Long id;
  private NotificationType type;
  private String title;
  private String message;
  private Long referenceId;
  private String referenceType;
  private NotificationStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime readAt;
}
