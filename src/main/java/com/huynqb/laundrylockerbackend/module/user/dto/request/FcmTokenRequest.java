package com.huynqb.laundrylockerbackend.module.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for registering/updating FCM token for push notifications. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {

  @NotBlank(message = "FCM token is required")
  private String fcmToken;

  /** Device type: ANDROID, IOS, WEB */
  private String deviceType;

  /** Optional device identifier for managing multiple devices. */
  private String deviceId;
}
