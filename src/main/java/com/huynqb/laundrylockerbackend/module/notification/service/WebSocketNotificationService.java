package com.huynqb.laundrylockerbackend.module.notification.service;

import com.huynqb.laundrylockerbackend.module.notification.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending real-time notifications via WebSocket. Uses STOMP protocol over WebSocket.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

  private final SimpMessagingTemplate messagingTemplate;

  /** Destination for user-specific notifications */
  private static final String USER_NOTIFICATION_DESTINATION = "/queue/notifications";

  /** Destination for broadcast notifications */
  private static final String BROADCAST_DESTINATION = "/topic/notifications";

  /**
   * Send notification to a specific user. Client should subscribe to:
   * /user/{userId}/queue/notifications
   *
   * @param userId User ID to send notification to
   * @param notification Notification data
   */
  public void sendToUser(Long userId, NotificationResponse notification) {
    try {
      String destination = USER_NOTIFICATION_DESTINATION;
      messagingTemplate.convertAndSendToUser(userId.toString(), destination, notification);
      log.debug("WebSocket notification sent to user {}: {}", userId, notification.getTitle());
    } catch (Exception e) {
      log.error("Failed to send WebSocket notification to user {}: {}", userId, e.getMessage());
    }
  }

  /**
   * Broadcast notification to all connected clients. Client should subscribe to:
   * /topic/notifications
   *
   * @param notification Notification data
   */
  public void broadcast(NotificationResponse notification) {
    try {
      messagingTemplate.convertAndSend(BROADCAST_DESTINATION, notification);
      log.debug("WebSocket broadcast notification sent: {}", notification.getTitle());
    } catch (Exception e) {
      log.error("Failed to broadcast WebSocket notification: {}", e.getMessage());
    }
  }

  /**
   * Send notification to a specific destination.
   *
   * @param destination STOMP destination
   * @param payload Notification payload
   */
  public void sendToDestination(String destination, Object payload) {
    try {
      messagingTemplate.convertAndSend(destination, payload);
      log.debug("WebSocket message sent to {}", destination);
    } catch (Exception e) {
      log.error("Failed to send WebSocket message to {}: {}", destination, e.getMessage());
    }
  }
}
