package com.huynqb.laundrylockerbackend.module.admin.service;

import com.huynqb.laundrylockerbackend.module.admin.dto.request.AdminBroadcastNotificationRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.AdminSendNotificationRequest;
import com.huynqb.laundrylockerbackend.module.notification.dto.response.NotificationResponse;
import com.huynqb.laundrylockerbackend.module.notification.enums.NotificationType;
import com.huynqb.laundrylockerbackend.module.notification.mapper.NotificationMapper;
import com.huynqb.laundrylockerbackend.module.notification.model.Notification;
import com.huynqb.laundrylockerbackend.module.notification.repository.NotificationRepository;
import com.huynqb.laundrylockerbackend.module.notification.service.FcmPushNotificationService;
import com.huynqb.laundrylockerbackend.module.notification.service.NotificationService;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for admin notification management operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final NotificationService notificationService;
  private final FcmPushNotificationService fcmPushNotificationService;
  private final UserRepository userRepository;

  /**
   * Get all notifications (admin view) with optional filters.
   *
   * @param type Optional notification type filter
   * @param userId Optional user ID filter
   * @param pageable Pagination info
   * @return Paginated notifications
   */
  @Transactional(readOnly = true)
  public Page<NotificationResponse> getAllNotifications(
      NotificationType type, Long userId, Pageable pageable) {

    Page<Notification> notifications;

    if (userId != null) {
      notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    } else if (type != null) {
      notifications = notificationRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
    } else {
      notifications = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    return notifications.map(notificationMapper::toResponse);
  }

  /**
   * Send notification to a specific user (admin action).
   *
   * @param request Send notification request
   * @return Created notification response
   */
  @Transactional
  public NotificationResponse sendToUser(AdminSendNotificationRequest request) {
    String typeString = request.getType() != null ? request.getType() : "SYSTEM";

    NotificationResponse response =
        notificationService.createNotification(
            request.getUserId(),
            typeString,
            request.getTitle(),
            request.getMessage(),
            request.getReferenceId(),
            request.getReferenceType());

    log.info("Admin sent notification to user {}: {}", request.getUserId(), request.getTitle());
    return response;
  }

  /**
   * Broadcast notification to all users (admin action). Creates a notification for each user and
   * sends via WebSocket + FCM push.
   *
   * @param request Broadcast notification request
   * @return Number of users notified
   */
  @Transactional
  public int broadcastToAll(AdminBroadcastNotificationRequest request) {
    String typeString = request.getType() != null ? request.getType() : "SYSTEM";

    var allUsers = userRepository.findAll();
    int count = 0;

    for (User user : allUsers) {
      try {
        notificationService.createNotification(
            user.getId(), typeString, request.getTitle(), request.getMessage(), null, null);
        count++;
      } catch (Exception e) {
        log.warn(
            "Failed to create broadcast notification for user {}: {}",
            user.getId(),
            e.getMessage());
      }
    }

    // Also send FCM broadcast for real-time delivery
    sendFcmBroadcastAsync(request.getTitle(), request.getMessage());

    log.info("Admin broadcast notification sent to {} users: {}", count, request.getTitle());
    return count;
  }

  /** Send FCM broadcast asynchronously. */
  @Async
  protected void sendFcmBroadcastAsync(String title, String message) {
    try {
      fcmPushNotificationService.broadcastToAll(title, message, Map.of("type", "BROADCAST"));
    } catch (Exception e) {
      log.warn("Failed to send FCM broadcast: {}", e.getMessage());
    }
  }
}
