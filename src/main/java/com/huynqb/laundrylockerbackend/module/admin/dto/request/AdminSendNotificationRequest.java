package com.huynqb.laundrylockerbackend.module.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request DTO for sending a notification to a specific user. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSendNotificationRequest {

  @NotNull(message = "User ID is required")
  private Long userId;

  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Message is required")
  private String message;

  /** Notification type (e.g., SYSTEM, PROMOTION). Defaults to SYSTEM if not provided. */
  private String type;

  /** Optional reference ID for navigation (orderId, paymentId, etc.). */
  private Long referenceId;

  /** Optional reference type for navigation (ORDER, PAYMENT, etc.). */
  private String referenceType;
}
