package com.huynqb.laundrylockerbackend.module.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.huynqb.laundrylockerbackend.module.notification.model.FcmToken;
import com.huynqb.laundrylockerbackend.module.notification.repository.FcmTokenRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending push notifications via Firebase Cloud Messaging (FCM). Handles single user,
 * single token, and broadcast notifications with automatic stale token cleanup.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushNotificationService {

  private final FcmTokenRepository fcmTokenRepository;

  /** Maximum tokens per FCM multicast request. */
  private static final int FCM_MULTICAST_LIMIT = 500;

  /**
   * Send push notification to all devices of a specific user.
   *
   * @param userId User ID
   * @param title Notification title
   * @param body Notification body
   * @param data Additional data payload
   */
  @Async
  public void sendToUser(Long userId, String title, String body, Map<String, String> data) {
    if (!isFirebaseAvailable()) {
      log.debug("Firebase not available, skipping FCM push for user {}", userId);
      return;
    }

    List<FcmToken> tokens = fcmTokenRepository.findByUserId(userId);
    if (tokens.isEmpty()) {
      log.debug("No FCM tokens found for user {}", userId);
      return;
    }

    List<String> tokenStrings = tokens.stream().map(FcmToken::getToken).toList();
    sendMulticast(tokenStrings, title, body, data);
    log.debug("FCM push sent to user {} ({} devices)", userId, tokenStrings.size());
  }

  /**
   * Send push notification to a single FCM token.
   *
   * @param token FCM token
   * @param title Notification title
   * @param body Notification body
   * @param data Additional data payload
   */
  public void sendToToken(String token, String title, String body, Map<String, String> data) {
    if (!isFirebaseAvailable()) {
      log.debug("Firebase not available, skipping FCM push to token");
      return;
    }

    try {
      Message.Builder messageBuilder =
          Message.builder()
              .setToken(token)
              .setNotification(
                  com.google.firebase.messaging.Notification.builder()
                      .setTitle(title)
                      .setBody(body)
                      .build());

      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
      log.debug("FCM message sent successfully: {}", response);
    } catch (FirebaseMessagingException e) {
      handleSingleTokenError(token, e);
    }
  }

  /**
   * Broadcast push notification to all registered devices.
   *
   * @param title Notification title
   * @param body Notification body
   * @param data Additional data payload
   */
  @Async
  public void broadcastToAll(String title, String body, Map<String, String> data) {
    if (!isFirebaseAvailable()) {
      log.debug("Firebase not available, skipping FCM broadcast");
      return;
    }

    List<String> allTokens = fcmTokenRepository.findAllDistinctTokens();
    if (allTokens.isEmpty()) {
      log.debug("No FCM tokens found for broadcast");
      return;
    }

    // Batch tokens in groups of FCM_MULTICAST_LIMIT (500)
    for (int i = 0; i < allTokens.size(); i += FCM_MULTICAST_LIMIT) {
      List<String> batch =
          allTokens.subList(i, Math.min(i + FCM_MULTICAST_LIMIT, allTokens.size()));
      sendMulticast(batch, title, body, data);
    }

    log.info("FCM broadcast sent to {} tokens", allTokens.size());
  }

  /**
   * Send multicast message to a list of tokens. Handles stale token cleanup automatically.
   *
   * @param tokens List of FCM tokens
   * @param title Notification title
   * @param body Notification body
   * @param data Additional data payload
   */
  private void sendMulticast(
      List<String> tokens, String title, String body, Map<String, String> data) {
    try {
      MulticastMessage.Builder messageBuilder =
          MulticastMessage.builder()
              .addAllTokens(tokens)
              .setNotification(
                  com.google.firebase.messaging.Notification.builder()
                      .setTitle(title)
                      .setBody(body)
                      .build());

      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      BatchResponse batchResponse =
          FirebaseMessaging.getInstance().sendEachForMulticast(messageBuilder.build());

      // Handle stale tokens
      if (batchResponse.getFailureCount() > 0) {
        handleStaleTokens(tokens, batchResponse.getResponses());
      }

      log.debug(
          "FCM multicast: {} success, {} failed",
          batchResponse.getSuccessCount(),
          batchResponse.getFailureCount());
    } catch (FirebaseMessagingException e) {
      log.error("FCM multicast failed: {}", e.getMessage());
    }
  }

  /**
   * Remove stale/invalid FCM tokens from the database. Tokens that return UNREGISTERED or
   * INVALID_ARGUMENT are considered stale.
   */
  private void handleStaleTokens(List<String> tokens, List<SendResponse> responses) {
    List<String> staleTokens = new ArrayList<>();

    for (int i = 0; i < responses.size(); i++) {
      SendResponse response = responses.get(i);
      if (!response.isSuccessful() && response.getException() != null) {
        MessagingErrorCode errorCode = response.getException().getMessagingErrorCode();
        if (errorCode == MessagingErrorCode.UNREGISTERED
            || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
          staleTokens.add(tokens.get(i));
        }
      }
    }

    if (!staleTokens.isEmpty()) {
      for (String staleToken : staleTokens) {
        fcmTokenRepository.findByToken(staleToken).ifPresent(fcmTokenRepository::delete);
      }
      log.info("Removed {} stale FCM tokens", staleTokens.size());
    }
  }

  /** Handle error for a single token send. */
  private void handleSingleTokenError(String token, FirebaseMessagingException e) {
    MessagingErrorCode errorCode = e.getMessagingErrorCode();
    if (errorCode == MessagingErrorCode.UNREGISTERED
        || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
      fcmTokenRepository.findByToken(token).ifPresent(fcmTokenRepository::delete);
      log.info("Removed stale FCM token after send failure");
    } else {
      log.error("FCM send failed for token: {} - {}", token, e.getMessage());
    }
  }

  /** Check if Firebase is available (initialized). */
  private boolean isFirebaseAvailable() {
    try {
      return !FirebaseApp.getApps().isEmpty();
    } catch (Exception e) {
      return false;
    }
  }
}
