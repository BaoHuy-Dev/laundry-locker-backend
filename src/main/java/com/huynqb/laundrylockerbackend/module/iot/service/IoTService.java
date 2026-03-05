package com.huynqb.laundrylockerbackend.module.iot.service;

import com.huynqb.laundrylockerbackend.module.iot.dto.request.BoxStatusUpdateRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.PickupRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.UnlockBoxRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.VerifyPinRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.PickupResponse;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.UnlockBoxResponse;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.VerifyPinResponse;
import com.huynqb.laundrylockerbackend.module.iot.mapper.IoTMapper;
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
  private final IoTMapper ioTMapper;
  private final LockerMqttService lockerMqttService;

  /** Verify PIN code for a specific box. Returns order information if PIN is valid. */
  @Transactional(readOnly = true)
  public VerifyPinResponse verifyPin(VerifyPinRequest request) {
    log.info("Verifying PIN for box: {}", request.getBoxId());

    Box box = boxRepository.findById(request.getBoxId()).orElse(null);

    if (box == null) {
      return ioTMapper.toVerifyError(null, "Box not found");
    }

    Optional<Order> orderOpt = orderRepository.findByPinCode(request.getPinCode());

    if (orderOpt.isEmpty()) {
      return ioTMapper.toVerifyError(request.getBoxId(), "Invalid PIN code");
    }

    Order order = orderOpt.get();

    // Check if the PIN matches the correct box
    boolean isValidBox = isValidBoxForOrder(order, box);

    if (!isValidBox) {
      return ioTMapper.toVerifyError(request.getBoxId(), "PIN code does not match this box");
    }

    return ioTMapper.toVerifySuccess(order, box);
  }

  /** Unlock a box using PIN code. Generates an unlock token for IoT device verification. */
  @Transactional
  public UnlockBoxResponse unlockBox(UnlockBoxRequest request) {
    log.info("Unlocking box: {} with PIN", request.getBoxId());

    Box box = boxRepository.findById(request.getBoxId()).orElse(null);

    if (box == null) {
      return ioTMapper.toUnlockError(null, "Box not found");
    }

    Optional<Order> orderOpt = orderRepository.findByPinCode(request.getPinCode());

    if (orderOpt.isEmpty()) {
      return ioTMapper.toUnlockError(request.getBoxId(), "Invalid PIN code");
    }

    Order order = orderOpt.get();

    // Validate PIN matches the box
    if (!isValidBoxForOrder(order, box)) {
      return ioTMapper.toUnlockError(request.getBoxId(), "PIN code does not match this box");
    }

    // Generate unlock token for IoT device
    String unlockToken = UUID.randomUUID().toString();

    // Publish MQTT unlock command to ESP8266
    try {
      String deviceId = box.getLocker().getCode(); // Use locker code as device ID
      lockerMqttService.sendUnlockCommand(deviceId, box.getBoxNumber());
    } catch (Exception e) {
      log.error("Failed to send MQTT unlock command for box {}: {}", box.getId(), e.getMessage());
      // Don't fail the unlock response - MQTT is best-effort
    }

    log.info("Box {} unlocked for order {}", box.getId(), order.getId());

    return ioTMapper.toUnlockSuccess(order, box, unlockToken);
  }

  /** Complete customer pickup - mark order as COMPLETED and release box. */
  @Transactional
  public PickupResponse confirmPickup(PickupRequest request, Long userId) {
    log.info("Confirming pickup for order: {} by user: {}", request.getOrderId(), userId);

    Order order = orderRepository.findById(request.getOrderId()).orElse(null);

    if (order == null) {
      return ioTMapper.toPickupError(null, null, "Order not found");
    }

    // Validate order belongs to user
    if (!order.getSender().getId().equals(userId)) {
      return ioTMapper.toPickupError(
          request.getOrderId(), null, "Order does not belong to this user");
    }

    // Validate order status - must be RETURNED (after payment) or COMPLETED
    // (already completed)
    if (order.getStatus() != OrderStatus.RETURNED && order.getStatus() != OrderStatus.COMPLETED) {
      return ioTMapper.toPickupError(
          request.getOrderId(),
          order.getStatus().name(),
          "Order is not ready for pickup. Current status: " + order.getStatus());
    }

    // If already completed, just return success
    if (order.getStatus() == OrderStatus.COMPLETED) {
      return ioTMapper.toPickupSuccess(
          order.getId(),
          order.getStatus().name(),
          order.getCompletedAt(),
          "Order already completed");
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

    return ioTMapper.toPickupSuccess(
        order.getId(), OrderStatus.COMPLETED.name(), now, "Pickup confirmed. Order completed!");
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
