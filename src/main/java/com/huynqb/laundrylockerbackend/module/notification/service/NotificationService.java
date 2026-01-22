package com.huynqb.laundrylockerbackend.module.notification.service;

import com.huynqb.laundrylockerbackend.module.notification.dto.response.NotificationResponse;
import com.huynqb.laundrylockerbackend.module.notification.enums.NotificationStatus;
import com.huynqb.laundrylockerbackend.module.notification.enums.NotificationType;
import com.huynqb.laundrylockerbackend.module.notification.mapper.NotificationMapper;
import com.huynqb.laundrylockerbackend.module.notification.model.Notification;
import com.huynqb.laundrylockerbackend.module.notification.repository.NotificationRepository;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing notifications. Handles creating, retrieving, and updating notification
 * status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final WebSocketNotificationService webSocketNotificationService;

  // ==================== Create Notifications ====================

  /** Send notification when order status changes. */
  @Transactional
  public void sendOrderStatusNotification(
      Order order, OrderStatus oldStatus, OrderStatus newStatus) {
    User user = order.getSender();
    if (user == null) {
      log.warn("Cannot send notification: order {} has no sender", order.getId());
      return;
    }

    String title = getOrderStatusTitle(newStatus);
    String message = getOrderStatusMessage(order.getId(), newStatus);

    Notification notification =
        createAndSaveNotification(
            user, NotificationType.ORDER_STATUS, title, message, order.getId(), "ORDER");

    // Push real-time via WebSocket
    NotificationResponse response = notificationMapper.toResponse(notification);
    webSocketNotificationService.sendToUser(user.getId(), response);

    log.info(
        "Order status notification sent to user {} for order {}: {} -> {}",
        user.getId(),
        order.getId(),
        oldStatus,
        newStatus);
  }

  /** Send notification when payment is completed. */
  @Transactional
  public void sendPaymentNotification(Payment payment, boolean success) {
    User user = payment.getCustomer();
    if (user == null) {
      log.warn("Cannot send notification: payment {} has no customer", payment.getId());
      return;
    }

    String title = success ? "Thanh toán thành công" : "Thanh toán thất bại";
    String message =
        success
            ? String.format(
                "Đơn hàng #%d đã được thanh toán thành công.", payment.getOrder().getId())
            : String.format(
                "Thanh toán cho đơn hàng #%d thất bại. Vui lòng thử lại.",
                payment.getOrder().getId());

    Notification notification =
        createAndSaveNotification(
            user, NotificationType.PAYMENT, title, message, payment.getId(), "PAYMENT");

    NotificationResponse response = notificationMapper.toResponse(notification);
    webSocketNotificationService.sendToUser(user.getId(), response);

    log.info(
        "Payment notification sent to user {} for payment {}: success={}",
        user.getId(),
        payment.getId(),
        success);
  }

  /** Send system notification to a user. */
  @Transactional
  public void sendSystemNotification(User user, String title, String message) {
    Notification notification =
        createAndSaveNotification(user, NotificationType.SYSTEM, title, message, null, null);

    NotificationResponse response = notificationMapper.toResponse(notification);
    webSocketNotificationService.sendToUser(user.getId(), response);

    log.info("System notification sent to user {}: {}", user.getId(), title);
  }

  // ==================== Retrieve Notifications ====================

  /** Get all notifications for a user (paginated). */
  public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
    return notificationRepository
        .findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(notificationMapper::toResponse);
  }

  /** Get all notifications for a user. */
  public List<NotificationResponse> getAllNotifications(Long userId) {
    List<Notification> notifications =
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    return notificationMapper.toResponseList(notifications);
  }

  /** Get unread notifications for a user. */
  public List<NotificationResponse> getUnreadNotifications(Long userId) {
    List<Notification> notifications =
        notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
            userId, NotificationStatus.UNREAD);
    return notificationMapper.toResponseList(notifications);
  }

  /** Get unread notification count for a user. */
  public long getUnreadCount(Long userId) {
    return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
  }

  // ==================== Update Notifications ====================

  /** Mark a notification as read. */
  @Transactional
  public NotificationResponse markAsRead(Long notificationId, Long userId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(
                () -> new IllegalArgumentException("Notification not found: " + notificationId));

    // Security check: ensure user owns the notification
    if (!notification.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Access denied to notification: " + notificationId);
    }

    if (notification.getStatus() == NotificationStatus.UNREAD) {
      notification.setStatus(NotificationStatus.READ);
      notification.setReadAt(LocalDateTime.now());
      notification = notificationRepository.save(notification);
    }

    return notificationMapper.toResponse(notification);
  }

  /** Mark all notifications as read for a user. */
  @Transactional
  public int markAllAsRead(Long userId) {
    return notificationRepository.markAllAsRead(
        userId, NotificationStatus.READ, NotificationStatus.UNREAD);
  }

  /** Delete a notification. */
  @Transactional
  public void deleteNotification(Long notificationId, Long userId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(
                () -> new IllegalArgumentException("Notification not found: " + notificationId));

    if (!notification.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Access denied to notification: " + notificationId);
    }

    notificationRepository.delete(notification);
    log.info("Notification {} deleted by user {}", notificationId, userId);
  }

  // ==================== Private Helper Methods ====================

  private Notification createAndSaveNotification(
      User user,
      NotificationType type,
      String title,
      String message,
      Long referenceId,
      String referenceType) {

    Notification notification =
        Notification.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .status(NotificationStatus.UNREAD)
            .createdAt(LocalDateTime.now())
            .build();

    return notificationRepository.save(notification);
  }

  private String getOrderStatusTitle(OrderStatus status) {
    return switch (status) {
      case INITIALIZED -> "Đơn hàng đã tạo";
      case WAITING -> "Đơn hàng đang chờ xử lý";
      case COLLECTED -> "Đã lấy đồ giặt";
      case PROCESSING -> "Đang xử lý giặt";
      case READY -> "Đồ giặt đã sẵn sàng";
      case RETURNED -> "Đồ đã được trả vào tủ";
      case COMPLETED -> "Đơn hàng hoàn thành";
      case CANCELED -> "Đơn hàng đã hủy";
      default -> "Cập nhật đơn hàng";
    };
  }

  private String getOrderStatusMessage(Long orderId, OrderStatus status) {
    return switch (status) {
      case INITIALIZED ->
          String.format("Đơn hàng #%d đã được tạo. Vui lòng đặt đồ vào tủ.", orderId);
      case WAITING -> String.format("Đơn hàng #%d đang chờ nhân viên đến lấy.", orderId);
      case COLLECTED -> String.format("Nhân viên đã lấy đồ từ đơn hàng #%d.", orderId);
      case PROCESSING -> String.format("Đơn hàng #%d đang được xử lý giặt.", orderId);
      case READY -> String.format("Đồ giặt đơn hàng #%d đã sẵn sàng để trả.", orderId);
      case RETURNED ->
          String.format("Đồ giặt đã được trả vào tủ. Vui lòng đến nhận đơn hàng #%d.", orderId);
      case COMPLETED ->
          String.format("Đơn hàng #%d đã hoàn thành. Cảm ơn bạn đã sử dụng dịch vụ!", orderId);
      case CANCELED -> String.format("Đơn hàng #%d đã bị hủy.", orderId);
      default -> String.format("Đơn hàng #%d đã được cập nhật.", orderId);
    };
  }
}
