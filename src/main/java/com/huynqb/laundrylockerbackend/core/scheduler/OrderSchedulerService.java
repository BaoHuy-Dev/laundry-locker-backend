package com.huynqb.laundrylockerbackend.core.scheduler;

import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.notification.service.NotificationService;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduler service for automated order and box management tasks.
 *
 * <p>Tasks: 1. Auto-cancel orders that are not confirmed within timeout period 2. Auto-release
 * boxes after order completion 3. Send reminder notifications for pending pickups
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSchedulerService {

  private final OrderRepository orderRepository;
  private final BoxRepository boxRepository;
  private final NotificationService notificationService;

  /** Timeout in minutes for auto-canceling unconfirmed orders. Default: 30 minutes */
  @Value("${app.scheduler.order-confirm-timeout-minutes:30}")
  private int orderConfirmTimeoutMinutes;

  /**
   * Delay in minutes before releasing boxes after order completion. Default: 5 minutes (grace
   * period)
   */
  @Value("${app.scheduler.box-release-delay-minutes:5}")
  private int boxReleaseDelayMinutes;

  /** Timeout in hours for sending pickup reminders. Default: 24 hours */
  @Value("${app.scheduler.pickup-reminder-hours:24}")
  private int pickupReminderHours;

  // ===== TASK 1: Auto-cancel unconfirmed orders =====

  /**
   * Scheduled task to auto-cancel orders that remain in INITIALIZED status for longer than the
   * configured timeout period.
   *
   * <p>Runs every 5 minutes.
   *
   * <p>Business logic: - Find orders with status INITIALIZED created more than 30 minutes ago -
   * Cancel them and release the reserved boxes - Send notification to customer about
   * auto-cancellation
   */
  @Scheduled(fixedRateString = "${app.scheduler.auto-cancel-rate-ms:300000}") // Default: 5 minutes
  @Transactional
  public void autoCancelUnconfirmedOrders() {
    log.debug("Running auto-cancel job for unconfirmed orders...");

    LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(orderConfirmTimeoutMinutes);

    List<Order> expiredOrders =
        orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.INITIALIZED, cutoffTime);

    if (expiredOrders.isEmpty()) {
      log.debug("No expired orders found for auto-cancellation");
      return;
    }

    log.info("Found {} orders to auto-cancel", expiredOrders.size());

    int canceledCount = 0;
    for (Order order : expiredOrders) {
      try {
        cancelOrderAndReleaseBox(order);
        sendAutoCancelNotification(order);
        canceledCount++;
        log.info("Auto-canceled order {} (created at: {})", order.getId(), order.getCreatedAt());
      } catch (Exception e) {
        log.error("Failed to auto-cancel order {}: {}", order.getId(), e.getMessage());
      }
    }

    log.info("Auto-cancel job completed. Canceled {} orders", canceledCount);
  }

  /** Cancel order and release the send box. */
  private void cancelOrderAndReleaseBox(Order order) {
    OrderStatus oldStatus = order.getStatus();

    // Release the send box
    Box sendBox = order.getSendBox();
    if (sendBox != null && sendBox.getStatus() == BoxStatus.OCCUPIED) {
      sendBox.setStatus(BoxStatus.AVAILABLE);
      boxRepository.save(sendBox);
      log.debug("Released box {} for auto-canceled order {}", sendBox.getId(), order.getId());
    }

    // Cancel the order
    order.setStatus(OrderStatus.CANCELED);
    order.setCancelReason(99); // 99 = System auto-cancel (timeout)
    orderRepository.save(order);
  }

  /** Send notification to customer about auto-cancellation. */
  private void sendAutoCancelNotification(Order order) {
    try {
      notificationService.sendOrderStatusNotification(
          order, OrderStatus.INITIALIZED, OrderStatus.CANCELED);
    } catch (Exception e) {
      log.warn(
          "Failed to send auto-cancel notification for order {}: {}",
          order.getId(),
          e.getMessage());
    }
  }

  // ===== TASK 2: Auto-release boxes after completion =====

  /**
   * Scheduled task to release boxes from completed orders.
   *
   * <p>Runs every 2 minutes.
   *
   * <p>Business logic: - Find COMPLETED orders that still have boxes assigned - Release boxes after
   * a grace period (default 5 minutes) - Clear box references from the order
   */
  @Scheduled(fixedRateString = "${app.scheduler.box-release-rate-ms:120000}") // Default: 2 minutes
  @Transactional
  public void autoReleaseBoxesAfterCompletion() {
    log.debug("Running auto-release boxes job...");

    LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(boxReleaseDelayMinutes);

    List<Order> completedOrders = orderRepository.findCompletedOrdersWithBoxesBefore(cutoffTime);

    if (completedOrders.isEmpty()) {
      log.debug("No boxes to release");
      return;
    }

    log.info("Found {} completed orders with boxes to release", completedOrders.size());

    int releasedCount = 0;
    for (Order order : completedOrders) {
      try {
        releaseOrderBoxes(order);
        releasedCount++;
      } catch (Exception e) {
        log.error("Failed to release boxes for order {}: {}", order.getId(), e.getMessage());
      }
    }

    log.info("Box release job completed. Released boxes from {} orders", releasedCount);
  }

  /** Release all boxes associated with the order. */
  private void releaseOrderBoxes(Order order) {
    // Release send box
    Box sendBox = order.getSendBox();
    if (sendBox != null) {
      if (sendBox.getStatus() == BoxStatus.OCCUPIED || sendBox.getStatus() == BoxStatus.RESERVED) {
        sendBox.setStatus(BoxStatus.AVAILABLE);
        boxRepository.save(sendBox);
        log.debug("Released send box {} for completed order {}", sendBox.getId(), order.getId());
      }
      order.setSendBox(null);
    }

    // Release receive box
    Box receiveBox = order.getReceiveBox();
    if (receiveBox != null) {
      if (receiveBox.getStatus() == BoxStatus.OCCUPIED
          || receiveBox.getStatus() == BoxStatus.RESERVED) {
        receiveBox.setStatus(BoxStatus.AVAILABLE);
        boxRepository.save(receiveBox);
        log.debug(
            "Released receive box {} for completed order {}", receiveBox.getId(), order.getId());
      }
      order.setReceiveBox(null);
    }

    // Clear PIN code for security
    order.setPinCode(null);
    order.setPinCodeIssuedAt(null);

    orderRepository.save(order);
    log.info("Released all boxes for completed order {}", order.getId());
  }

  // ===== TASK 3: Send pickup reminders =====

  /**
   * Scheduled task to send reminder notifications for orders waiting pickup.
   *
   * <p>Runs every hour.
   *
   * <p>Business logic: - Find RETURNED orders that have been waiting more than 24 hours - Send
   * reminder notification to customer
   */
  @Scheduled(fixedRateString = "${app.scheduler.reminder-rate-ms:3600000}") // Default: 1 hour
  @Transactional(readOnly = true)
  public void sendPickupReminders() {
    log.debug("Running pickup reminder job...");

    LocalDateTime cutoffTime = LocalDateTime.now().minusHours(pickupReminderHours);

    List<Order> pendingPickupOrders = orderRepository.findReturnedOrdersBefore(cutoffTime);

    if (pendingPickupOrders.isEmpty()) {
      log.debug("No orders pending pickup reminder");
      return;
    }

    log.info(
        "Found {} orders pending pickup for more than {} hours",
        pendingPickupOrders.size(),
        pickupReminderHours);

    int remindersSent = 0;
    for (Order order : pendingPickupOrders) {
      try {
        sendPickupReminderNotification(order);
        remindersSent++;
      } catch (Exception e) {
        log.warn("Failed to send pickup reminder for order {}: {}", order.getId(), e.getMessage());
      }
    }

    log.info("Pickup reminder job completed. Sent {} reminders", remindersSent);
  }

  /** Send pickup reminder notification to customer. */
  private void sendPickupReminderNotification(Order order) {
    // Create a reminder-type notification
    String title = "Nhắc nhở lấy đồ";
    String message =
        String.format(
            "Đơn hàng #%d của bạn đã sẵn sàng hơn %d giờ. "
                + "Vui lòng đến lấy đồ tại tủ %s, ô số %d. Mã PIN: %s",
            order.getId(),
            pickupReminderHours,
            order.getLocker().getCode(),
            order.getReceiveBox() != null ? order.getReceiveBox().getBoxNumber() : 0,
            order.getPinCode());

    try {
      notificationService.createNotification(
          order.getSender().getId(), "PICKUP_REMINDER", title, message, order.getId(), "ORDER");
      log.debug("Sent pickup reminder for order {}", order.getId());
    } catch (Exception e) {
      log.warn("Failed to create pickup reminder notification: {}", e.getMessage());
    }
  }

  // ===== Manual Trigger Methods (for Admin/Testing) =====

  /** Manually trigger auto-cancel job. Can be called from admin controller for testing. */
  public int triggerAutoCancelJob() {
    log.info("Manually triggering auto-cancel job");
    autoCancelUnconfirmedOrders();
    return orderRepository.findByStatus(OrderStatus.CANCELED).size();
  }

  /** Manually trigger box release job. Can be called from admin controller for testing. */
  public int triggerBoxReleaseJob() {
    log.info("Manually triggering box release job");
    autoReleaseBoxesAfterCompletion();
    return 0; // Count not easily available
  }
}
