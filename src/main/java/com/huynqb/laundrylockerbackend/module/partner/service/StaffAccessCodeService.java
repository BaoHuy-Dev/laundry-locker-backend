package com.huynqb.laundrylockerbackend.module.partner.service;

import com.huynqb.laundrylockerbackend.core.util.CodeGenerator;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.notification.service.NotificationService;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.GenerateAccessCodeRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.StaffCodeUnlockRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.StaffAccessCodeResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.StaffCodeUnlockResponse;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeStatus;
import com.huynqb.laundrylockerbackend.module.partner.exception.PartnerException;
import com.huynqb.laundrylockerbackend.module.partner.helper.OrderBoxHelper;
import com.huynqb.laundrylockerbackend.module.partner.mapper.StaffAccessCodeMapper;
import com.huynqb.laundrylockerbackend.module.partner.model.Partner;
import com.huynqb.laundrylockerbackend.module.partner.model.StaffAccessCode;
import com.huynqb.laundrylockerbackend.module.partner.repository.PartnerRepository;
import com.huynqb.laundrylockerbackend.module.partner.repository.StaffAccessCodeRepository;
import com.huynqb.laundrylockerbackend.module.partner.validator.PartnerOrderValidator;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing staff access codes. Refactored following SOLID principles: - Single
 * Responsibility: Each method has one clear purpose - Open/Closed: Extended via composition with
 * helpers - Dependency Inversion: Depends on abstractions (interfaces)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffAccessCodeService {

  private final StaffAccessCodeRepository accessCodeRepository;
  private final PartnerRepository partnerRepository;
  private final OrderRepository orderRepository;
  private final NotificationService notificationService;
  private final StaffAccessCodeMapper accessCodeMapper;
  private final OrderBoxHelper orderBoxHelper;

  private static final int DEFAULT_EXPIRATION_HOURS = 24;

  // ===== Public API Methods =====

  /** Generate a new access code for an order. */
  @Transactional
  public StaffAccessCodeResponse generateAccessCode(
      Long partnerId, GenerateAccessCodeRequest request) {
    log.info("Generating access code for order {} by partner {}", request.getOrderId(), partnerId);

    Partner partner = findPartnerById(partnerId);
    Order order = findOrderById(request.getOrderId());

    // Validate
    PartnerOrderValidator.validateOrderBelongsToPartner(order, partnerId);
    PartnerOrderValidator.validateOrderStatusForAction(order, request.getAction());

    // Cancel existing active code if any
    cancelExistingActiveCode(request.getOrderId(), request.getAction());

    // Create new access code
    StaffAccessCode accessCode = createAccessCode(partner, order, request);
    accessCode = accessCodeRepository.save(accessCode);

    log.info(
        "Generated access code {} for order {} action {}",
        accessCode.getCode(),
        order.getId(),
        request.getAction());

    return accessCodeMapper.toResponse(accessCode);
  }

  /** Unlock box using staff access code. */
  @Transactional
  public StaffCodeUnlockResponse unlockWithCode(StaffCodeUnlockRequest request) {
    log.info("Attempting to unlock with code for order {}", request.getOrderId());

    // Find and validate the access code
    StaffAccessCode accessCode = findValidAccessCode(request.getAccessCode());
    if (accessCode == null) {
      return buildFailureResponse(request.getOrderId(), "Invalid or expired access code");
    }

    // Validate code matches the order
    if (!accessCode.getOrder().getId().equals(request.getOrderId())) {
      return buildFailureResponse(request.getOrderId(), "Access code does not match this order");
    }

    Order order = accessCode.getOrder();

    // Validate order status
    if (!PartnerOrderValidator.isValidStatusForUnlock(order, accessCode.getAction())) {
      return buildFailureResponse(
          order.getId(),
          order.getStatus().name(),
          "Order status is not valid for this action. Current: " + order.getStatus());
    }

    // Get boxes for action
    List<Box> boxes = orderBoxHelper.getBoxesForAction(order, accessCode.getAction());
    if (boxes.isEmpty()) {
      return buildFailureResponse(order.getId(), "No boxes found for this order");
    }

    // Process unlock
    processUnlock(accessCode, order, request.getStaffName());

    // Handle box status for return action
    if (accessCode.getAction() == AccessCodeAction.RETURN) {
      orderBoxHelper.markBoxesAsOccupied(boxes);
    }

    // Send notification
    sendNotificationAsync(order, accessCode.getAction());

    log.info(
        "Access code {} used successfully for order {} by {}",
        accessCode.getCode(),
        order.getId(),
        request.getStaffName());

    return buildSuccessResponse(order, accessCode.getAction(), boxes);
  }

  /** Get access codes by order ID. */
  @Transactional(readOnly = true)
  public List<StaffAccessCodeResponse> getCodesByOrderId(Long orderId) {
    return accessCodeMapper.toResponseList(
        accessCodeRepository.findByOrderIdOrderByCreatedAtDesc(orderId));
  }

  /** Get access codes by partner. */
  @Transactional(readOnly = true)
  public Page<StaffAccessCodeResponse> getCodesByPartner(Long partnerId, Pageable pageable) {
    return accessCodeRepository
        .findByPartnerIdOrderByCreatedAtDesc(partnerId, pageable)
        .map(accessCodeMapper::toResponse);
  }

  /** Cancel an access code. */
  @Transactional
  public void cancelAccessCode(Long codeId, Long partnerId) {
    StaffAccessCode code =
        accessCodeRepository
            .findById(codeId)
            .orElseThrow(() -> new RuntimeException("Access code not found"));

    if (!code.getPartner().getId().equals(partnerId)) {
      throw new RuntimeException("Access code does not belong to this partner");
    }

    if (code.getStatus() != AccessCodeStatus.ACTIVE) {
      throw new RuntimeException("Access code is not active");
    }

    code.cancel();
    accessCodeRepository.save(code);
    log.info("Access code {} cancelled by partner {}", code.getCode(), partnerId);
  }

  /** Cleanup expired codes (called by scheduler). */
  @Transactional
  public int cleanupExpiredCodes() {
    List<StaffAccessCode> expiredCodes = accessCodeRepository.findExpiredCodes(LocalDateTime.now());
    expiredCodes.forEach(code -> code.setStatus(AccessCodeStatus.EXPIRED));
    accessCodeRepository.saveAll(expiredCodes);
    log.info("Cleaned up {} expired access codes", expiredCodes.size());
    return expiredCodes.size();
  }

  // ===== Private Helper Methods =====

  private Partner findPartnerById(Long partnerId) {
    return partnerRepository.findById(partnerId).orElseThrow(PartnerException::notFound);
  }

  private Order findOrderById(Long orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
  }

  private StaffAccessCode findValidAccessCode(String code) {
    return accessCodeRepository.findValidCode(code, LocalDateTime.now()).orElse(null);
  }

  private void cancelExistingActiveCode(Long orderId, AccessCodeAction action) {
    accessCodeRepository
        .findActiveCodeForOrderAndAction(orderId, action, LocalDateTime.now())
        .ifPresent(
            existingCode -> {
              existingCode.cancel();
              accessCodeRepository.save(existingCode);
              log.info("Cancelled existing code {} for order {}", existingCode.getCode(), orderId);
            });
  }

  private StaffAccessCode createAccessCode(
      Partner partner, Order order, GenerateAccessCodeRequest request) {
    String code =
        CodeGenerator.generateUniqueCode(c -> accessCodeRepository.findByCode(c).isPresent());

    int expirationHours =
        request.getExpirationHours() != null
            ? request.getExpirationHours()
            : DEFAULT_EXPIRATION_HOURS;

    return StaffAccessCode.builder()
        .code(code)
        .order(order)
        .partner(partner)
        .action(request.getAction())
        .status(AccessCodeStatus.ACTIVE)
        .expiresAt(LocalDateTime.now().plusHours(expirationHours))
        .notes(request.getNotes())
        .build();
  }

  private void processUnlock(StaffAccessCode accessCode, Order order, String staffName) {
    // Mark code as used
    accessCode.markAsUsed(staffName);
    accessCodeRepository.save(accessCode);

    // Update order status and boxes
    orderBoxHelper.updateOrderAndBoxesAfterUnlock(order, accessCode.getAction());
    orderRepository.save(order);
  }

  private void sendNotificationAsync(Order order, AccessCodeAction action) {
    try {
      String title =
          action == AccessCodeAction.COLLECT ? "Đơn hàng đang được xử lý" : "Đồ đã sẵn sàng lấy";

      String message =
          action == AccessCodeAction.COLLECT
              ? String.format("Đơn hàng #%d đã được nhận và đang xử lý giặt.", order.getId())
              : String.format(
                  "Đơn hàng #%d đã được trả lại tủ. Mã PIN: %s", order.getId(), order.getPinCode());

      notificationService.sendSystemNotification(order.getSender(), title, message);
    } catch (Exception e) {
      log.warn("Failed to send notification for order {}: {}", order.getId(), e.getMessage());
    }
  }

  // ===== Response Builders =====

  private StaffCodeUnlockResponse buildFailureResponse(Long orderId, String message) {
    return StaffCodeUnlockResponse.builder()
        .success(false)
        .orderId(orderId)
        .message(message)
        .build();
  }

  private StaffCodeUnlockResponse buildFailureResponse(
      Long orderId, String status, String message) {
    return StaffCodeUnlockResponse.builder()
        .success(false)
        .orderId(orderId)
        .orderStatus(status)
        .message(message)
        .build();
  }

  private StaffCodeUnlockResponse buildSuccessResponse(
      Order order, AccessCodeAction action, List<Box> boxes) {
    return StaffCodeUnlockResponse.builder()
        .success(true)
        .orderId(order.getId())
        .orderStatus(order.getStatus().name())
        .action(action)
        .boxes(accessCodeMapper.toBoxInfoList(boxes))
        .lockerCode(order.getLocker().getCode())
        .lockerName(order.getLocker().getName())
        .lockerAddress(
            order.getLocker().getStore() != null ? order.getLocker().getStore().getAddress() : null)
        .unlockToken(CodeGenerator.generateToken())
        .unlockTimestamp(System.currentTimeMillis())
        .message("Box unlocked successfully")
        .build();
  }
}
