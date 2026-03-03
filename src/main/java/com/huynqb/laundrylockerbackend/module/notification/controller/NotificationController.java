package com.huynqb.laundrylockerbackend.module.notification.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.notification.dto.request.MarkReadRequest;
import com.huynqb.laundrylockerbackend.module.notification.dto.response.NotificationResponse;
import com.huynqb.laundrylockerbackend.module.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for notification management. */
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_NOTIFICATIONS)
@RequiredArgsConstructor
@Tag(name = TagConstants.ROOT_TAG_NOTIFICATIONS, description = "Notification Management APIs")
public class NotificationController {

  private final NotificationService notificationService;
  private final JwtTokenProvider jwtTokenProvider;
  private final ResponseHelper responseHelper;

  @GetMapping
  @Operation(summary = "Get all notifications for current user (paginated)")
  public ApiResponse<Page<NotificationResponse>> getNotifications(
      @RequestHeader("Authorization") String authHeader, Pageable pageable) {
    Long userId = extractUserId(authHeader);
    Page<NotificationResponse> notifications =
        notificationService.getNotifications(userId, pageable);
    return responseHelper.success(notifications, "NOTIFICATIONS_RETRIEVED");
  }

  @GetMapping(UriParamConstants.NOTIFICATIONS_ALL)
  @Operation(summary = "Get all notifications for current user")
  public ApiResponse<List<NotificationResponse>> getAllNotifications(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    List<NotificationResponse> notifications = notificationService.getAllNotifications(userId);
    return responseHelper.success(notifications, "NOTIFICATIONS_RETRIEVED");
  }

  @GetMapping(UriParamConstants.NOTIFICATIONS_UNREAD)
  @Operation(summary = "Get unread notifications for current user")
  public ApiResponse<List<NotificationResponse>> getUnreadNotifications(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
    return responseHelper.success(notifications, "NOTIFICATIONS_RETRIEVED");
  }

  @GetMapping(UriParamConstants.NOTIFICATIONS_UNREAD_COUNT)
  @Operation(summary = "Get unread notification count for current user")
  public ApiResponse<Map<String, Long>> getUnreadCount(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    long count = notificationService.getUnreadCount(userId);
    return responseHelper.success(Map.of("count", count), "UNREAD_COUNT_RETRIEVED");
  }

  @PutMapping(UriParamConstants.NOTIFICATIONS_READ)
  @Operation(summary = "Mark a notification as read")
  public ApiResponse<NotificationResponse> markAsRead(
      @PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    NotificationResponse notification = notificationService.markAsRead(id, userId);
    return responseHelper.success(notification, "NOTIFICATION_MARKED_READ");
  }

  @PutMapping(UriParamConstants.NOTIFICATIONS_READ_ALL)
  @Operation(summary = "Mark all notifications as read")
  public ApiResponse<Map<String, Integer>> markAllAsRead(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    int count = notificationService.markAllAsRead(userId);
    return responseHelper.success(Map.of("markedCount", count), "ALL_NOTIFICATIONS_MARKED_READ");
  }

  @PutMapping(UriParamConstants.NOTIFICATIONS_READ_BATCH)
  @Operation(summary = "Mark a batch of notifications as read")
  public ApiResponse<Map<String, Integer>> markBatchAsRead(
      @Valid @RequestBody MarkReadRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    int count = notificationService.markBatchAsRead(request.getNotificationIds(), userId);
    return responseHelper.success(Map.of("markedCount", count), "BATCH_NOTIFICATIONS_MARKED_READ");
  }

  @DeleteMapping(UriParamConstants.NOTIFICATIONS_DELETE_ALL)
  @Operation(summary = "Delete all notifications for current user")
  public ApiResponse<Void> deleteAllNotifications(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    notificationService.deleteAllNotifications(userId);
    return responseHelper.success("ALL_NOTIFICATIONS_DELETED");
  }

  @DeleteMapping(UriParamConstants.NOTIFICATIONS_DELETE)
  @Operation(summary = "Delete a notification")
  public ApiResponse<Void> deleteNotification(
      @PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    notificationService.deleteNotification(id, userId);
    return responseHelper.success("NOTIFICATION_DELETED");
  }

  private Long extractUserId(String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return jwtTokenProvider.getUserIdFromToken(token);
  }
}
