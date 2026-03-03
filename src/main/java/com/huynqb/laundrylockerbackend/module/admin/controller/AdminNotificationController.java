package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.AdminBroadcastNotificationRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.AdminSendNotificationRequest;
import com.huynqb.laundrylockerbackend.module.admin.service.AdminNotificationService;
import com.huynqb.laundrylockerbackend.module.notification.dto.response.NotificationResponse;
import com.huynqb.laundrylockerbackend.module.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for admin notification management. */
@Tag(
    name = TagConstants.ROOT_TAG_ADMIN_NOTIFICATIONS,
    description = "Admin Notification Management APIs")
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_NOTIFICATIONS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

  private final AdminNotificationService adminNotificationService;
  private final ResponseHelper responseHelper;

  /** Get all notifications with optional filters (Admin only). */
  @Operation(
      summary = "Get All Notifications",
      description = "Retrieve all notifications with optional type and userId filters (Admin only)")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getAllNotifications(
      @RequestParam(required = false) NotificationType type,
      @RequestParam(required = false) Long userId,
      Pageable pageable) {
    Page<NotificationResponse> notifications =
        adminNotificationService.getAllNotifications(type, userId, pageable);
    return ResponseEntity.ok(
        responseHelper.success(notifications, "ADMIN_NOTIFICATIONS_RETRIEVED"));
  }

  /** Send notification to a specific user (Admin only). */
  @Operation(
      summary = "Send Notification to User",
      description = "Send a notification to a specific user (Admin only)")
  @PostMapping(UriParamConstants.ADMIN_NOTIFICATION_SEND)
  public ResponseEntity<ApiResponse<NotificationResponse>> sendToUser(
      @Valid @RequestBody AdminSendNotificationRequest request) {
    NotificationResponse response = adminNotificationService.sendToUser(request);
    return ResponseEntity.ok(responseHelper.success(response, "ADMIN_NOTIFICATION_SENT"));
  }

  /** Broadcast notification to all users (Admin only). */
  @Operation(
      summary = "Broadcast Notification",
      description = "Send a notification to all users (Admin only)")
  @PostMapping(UriParamConstants.ADMIN_NOTIFICATION_BROADCAST)
  public ResponseEntity<ApiResponse<Map<String, Integer>>> broadcastToAll(
      @Valid @RequestBody AdminBroadcastNotificationRequest request) {
    int count = adminNotificationService.broadcastToAll(request);
    return ResponseEntity.ok(
        responseHelper.success(
            Map.of("notifiedCount", count), "ADMIN_NOTIFICATION_BROADCAST_SENT"));
  }
}
