package com.huynqb.laundrylockerbackend.module.iot.service;

import com.huynqb.laundrylockerbackend.module.iot.dto.request.BoxStatusUpdateRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.PickupRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.UnlockBoxRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.VerifyPinRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.PickupResponse;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.UnlockBoxResponse;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.VerifyPinResponse;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.notification.service.NotificationService;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for IoT operations - locker control and PIN verification. Handles box unlock/lock
 * operations and customer pickup flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IoTService {

  private final OrderRepository orderRepository;
  private final BoxRepository boxRepository;
  private final NotificationService notificationService;

  /** Verify PIN code for a specific box. Returns order information if PIN is valid. */
  @Transactional(readOnly = true)
  public VerifyPinResponse verifyPin(VerifyPinRequest request) {
    log.info("Verifying PIN for box: {}", request.getBoxId());

    Box box = boxRepository.findById(request.getBoxId()).orElse(null);

    if (box == null) {
      return VerifyPinResponse.builder().valid(false).message("Box not found").build();
    }

    Optional<Order> orderOpt = orderRepository.findByPinCode(request.getPinCode());

    if (orderOpt.isEmpty()) {
      return VerifyPinResponse.builder()
          .valid(false)
          .boxId(request.getBoxId())
          .message("Invalid PIN code")
          .build();
    }

    Order order = orderOpt.get();

    // Check if the PIN matches the correct box
    boolean isValidBox = isValidBoxForOrder(order, box);

    if (!isValidBox) {
      return VerifyPinResponse.builder()
          .valid(false)
          .boxId(request.getBoxId())
          .message("PIN code does not match this box")
          .build();
    }

    return VerifyPinResponse.builder()
        .valid(true)
        .orderId(order.getId())
        .boxId(box.getId())
        .boxNumber(box.getBoxNumber())
        .lockerCode(box.getLocker().getCode())
        .orderStatus(order.getStatus().name())
        .message("PIN verified successfully")
        .build();
  }

  /** Unlock a box using PIN code. Generates an unlock token for IoT device verification. */
  @Transactional
  public UnlockBoxResponse unlockBox(UnlockBoxRequest request) {
    log.info("Unlocking box: {} with PIN", request.getBoxId());

    Box box = boxRepository.findById(request.getBoxId()).orElse(null);

    if (box == null) {
      return UnlockBoxResponse.builder().success(false).message("Box not found").build();
    }

    Optional<Order> orderOpt = orderRepository.findByPinCode(request.getPinCode());

    if (orderOpt.isEmpty()) {
      return UnlockBoxResponse.builder()
          .success(false)
          .boxId(request.getBoxId())
          .message("Invalid PIN code")
          .build();
    }

    Order order = orderOpt.get();

    // Validate PIN matches the box
    if (!isValidBoxForOrder(order, box)) {
      return UnlockBoxResponse.builder()
          .success(false)
          .boxId(request.getBoxId())
          .message("PIN code does not match this box")
          .build();
    }

    // Generate unlock token for IoT device
    String unlockToken = UUID.randomUUID().toString();

    log.info("Box {} unlocked for order {}", box.getId(), order.getId());

    return UnlockBoxResponse.builder()
        .success(true)
        .boxId(box.getId())
        .boxNumber(box.getBoxNumber())
        .lockerCode(box.getLocker().getCode())
        .orderId(order.getId())
        .unlockToken(unlockToken)
        .unlockTimestamp(System.currentTimeMillis())
        .message("Box unlocked successfully")
        .build();
  }

  /** Complete customer pickup - mark order as COMPLETED and release box. */
  @Transactional
  public PickupResponse confirmPickup(PickupRequest request, Long userId) {
    log.info("Confirming pickup for order: {} by user: {}", request.getOrderId(), userId);

    Order order = orderRepository.findById(request.getOrderId()).orElse(null);

    if (order == null) {
      return PickupResponse.builder().success(false).message("Order not found").build();
    }

    // Validate order belongs to user
    if (!order.getSender().getId().equals(userId)) {
      return PickupResponse.builder()
          .success(false)
          .orderId(request.getOrderId())
          .message("Order does not belong to this user")
          .build();
    }

    // Validate order status - must be RETURNED (after payment) or COMPLETED (already completed)
    if (order.getStatus() != OrderStatus.RETURNED && order.getStatus() != OrderStatus.COMPLETED) {
      return PickupResponse.builder()
          .success(false)
          .orderId(request.getOrderId())
          .orderStatus(order.getStatus().name())
          .message("Order is not ready for pickup. Current status: " + order.getStatus())
          .build();
    }

    // If already completed, just return success
    if (order.getStatus() == OrderStatus.COMPLETED) {
      return PickupResponse.builder()
          .success(true)
          .orderId(order.getId())
          .orderStatus(order.getStatus().name())
          .completedAt(order.getCompletedAt())
          .message("Order already completed")
          .build();
    }

    // Complete the order
    LocalDateTime now = LocalDateTime.now();
    order.setStatus(OrderStatus.COMPLETED);
    order.setCompletedAt(now);
    order.setPinCode(null); // Clear PIN after pickup

    // Release the receive box
    Box receiveBox = order.getReceiveBox();
    if (receiveBox != null) {
      receiveBox.setStatus(BoxStatus.AVAILABLE);
      boxRepository.save(receiveBox);
    }

    orderRepository.save(order);

    log.info("Order {} completed successfully", order.getId());

    return PickupResponse.builder()
        .success(true)
        .orderId(order.getId())
        .orderStatus(OrderStatus.COMPLETED.name())
        .completedAt(now)
        .message("Pickup confirmed. Order completed!")
        .build();
  }

  /** Update box status from IoT device/sensor. */
  @Transactional
  public void updateBoxStatus(BoxStatusUpdateRequest request) {
    log.info("Updating box status: {} to {}", request.getBoxId(), request.getStatus());

    Box box =
        boxRepository
            .findById(request.getBoxId())
            .orElseThrow(() -> new RuntimeException("Box not found: " + request.getBoxId()));

    try {
      BoxStatus newStatus = BoxStatus.valueOf(request.getStatus().toUpperCase());
      box.setStatus(newStatus);
      boxRepository.save(box);
      log.info("Box {} status updated to {}", box.getId(), newStatus);
    } catch (IllegalArgumentException e) {
      log.error("Invalid box status: {}", request.getStatus());
      throw new RuntimeException("Invalid box status: " + request.getStatus());
    }
  }

  /** Check if the box matches the order's send or receive box. */
  private boolean isValidBoxForOrder(Order order, Box box) {
    // For INITIALIZED status - check send box (customer dropping off)
    if (order.getStatus() == OrderStatus.INITIALIZED) {
      return order.getSendBox() != null && order.getSendBox().getId().equals(box.getId());
    }

    // For RETURNED status - check receive box (customer picking up)
    if (order.getStatus() == OrderStatus.RETURNED) {
      return order.getReceiveBox() != null && order.getReceiveBox().getId().equals(box.getId());
    }

    // For other statuses, check both boxes
    boolean matchesSendBox =
        order.getSendBox() != null && order.getSendBox().getId().equals(box.getId());
    boolean matchesReceiveBox =
        order.getReceiveBox() != null && order.getReceiveBox().getId().equals(box.getId());

    return matchesSendBox || matchesReceiveBox;
  }
}
