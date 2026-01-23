package com.huynqb.laundrylockerbackend.module.notification.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request DTO for marking notifications as read. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkReadRequest {

  @NotEmpty(message = "Notification IDs cannot be empty")
  private List<Long> notificationIds;
}
