package com.huynqb.laundrylockerbackend.module.notification.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


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
