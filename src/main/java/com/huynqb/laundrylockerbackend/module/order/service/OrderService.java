package com.huynqb.laundrylockerbackend.module.order.service;

import com.huynqb.laundrylockerbackend.module.admin.model.Promotion;
import com.huynqb.laundrylockerbackend.module.admin.model.PromotionUsage;
import com.huynqb.laundrylockerbackend.module.admin.repository.PromotionRepository;
import com.huynqb.laundrylockerbackend.module.admin.repository.PromotionUsageRepository;
import com.huynqb.laundrylockerbackend.module.iot.service.LockerMqttService;
import com.huynqb.laundrylockerbackend.module.laundry.model.LaundryService;
import com.huynqb.laundrylockerbackend.module.laundry.repository.LaundryServiceRepository;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerRepository;
import com.huynqb.laundrylockerbackend.module.notification.service.NotificationService;
import com.huynqb.laundrylockerbackend.module.order.dto.request.CheckoutOrderRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.request.CreateOrderRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.request.OrderItemRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.request.UpdateOrderWeightRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderStatusResponse;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.exception.OrderException;
import com.huynqb.laundrylockerbackend.module.order.mapper.OrderMapper;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.model.OrderDetail;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentResponse;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentStatus;
import com.huynqb.laundrylockerbackend.module.payment.mapper.PaymentMapper;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import com.huynqb.laundrylockerbackend.module.payment.repository.PaymentRepository;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for order management operations. Follows SRP by delegating mapping to OrderMapper.
 * Follows OCP by using strategy pattern for order status validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final LockerRepository lockerRepository;
  private final BoxRepository boxRepository;
  private final LaundryServiceRepository laundryServiceRepository;
  private final PaymentRepository paymentRepository;
  private final UserRepository userRepository;
  private final PromotionRepository promotionRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final LockerMqttService lockerMqttService;

  private final OrderMapper orderMapper;
  private final PaymentMapper paymentMapper;
  private final NotificationService notificationService;

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int PIN_CODE_LENGTH = 6;
  private static final int PIN_CODE_BOUND = 1000000;

  // ===== Pickup Overtime Configuration =====
  @Value("${app.order.pickup-hours-limit:24}")
  private int pickupHoursLimit;

  @Value("${app.order.pickup-overtime-fee-per-hour:500}")
  private int overtimeFeePerHour;

  @Value("${app.order.pickup-max-overtime-fee:50000}")
  private int maxOvertimeFee;

  @Value("${app.order.pickup-max-overtime-percent:50}")
  private int maxOvertimePercent;

  // Valid statuses for checkout (OCP - extend by adding to list)
  private static final List<OrderStatus> CHECKOUT_VALID_STATUSES =
      List.of(OrderStatus.WAITING, OrderStatus.RETURNED, OrderStatus.READY);

  // Valid statuses for cancellation
  private static final List<OrderStatus> CANCEL_VALID_STATUSES =
      List.of(OrderStatus.INITIALIZED, OrderStatus.RESERVED, OrderStatus.WAITING);

  // ===== Create Order =====

  @Transactional
  public OrderResponse createOrder(CreateOrderRequest request, Long senderId) {
    log.info("Creating order for sender: {}, locker: {}", senderId, request.getLockerId());

    User sender = findUserById(senderId);
    Locker locker = findLockerById(request.getLockerId());

    // Handle boxIds - auto-assign if empty
    Set<Box> sendBoxes = new java.util.LinkedHashSet<>();
    Box primarySendBox;

    if (request.getBoxIds() != null && !request.getBoxIds().isEmpty()) {
      // Use specified boxes
      for (Long boxId : request.getBoxIds()) {
        Box box = findBoxById(boxId);
        validateBoxAvailable(box);
        box.setStatus(BoxStatus.OCCUPIED);
        boxRepository.save(box);
        sendBoxes.add(box);
      }
      primarySendBox = sendBoxes.iterator().next();
    } else {
      // Auto-assign an available box
      primarySendBox = findAvailableBox(locker.getId());
      primarySendBox.setStatus(BoxStatus.OCCUPIED);
      boxRepository.save(primarySendBox);
      sendBoxes.add(primarySendBox);
    }

    Order order = buildOrder(request, sender, locker, primarySendBox);

    // Set all send boxes (works for both single and multiple)
    order.setSendBoxes(sendBoxes);

    // Handle serviceIds (new) or items (deprecated)
    if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
      addOrderDetailsFromServiceIds(order, request.getServiceIds(), request.getEstimatedWeight());
    } else if (request.getItems() != null && !request.getItems().isEmpty()) {
      addOrderDetails(order, request.getItems());
    }

    // Apply promotion if provided
    applyPromotionToOrder(order, request.getPromotionCode(), request.getPromotionCodes());

    Order savedOrder = orderRepository.save(order);
    log.info("Order created: {} with PIN: {}", savedOrder.getId(), order.getPinCode());

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Checkout Order =====

  @Transactional
  public PaymentResponse checkoutOrder(Long orderId, CheckoutOrderRequest request, Long staffId) {
    log.info("Checkout order: {} by staff: {}", orderId, staffId);

    Order order = findOrderById(orderId);
    validateOrderStatus(order.getStatus(), CHECKOUT_VALID_STATUSES, "E_ORDER002");

    User staff = findUserById(staffId);
    Payment payment = createPayment(order, request);

    completeOrder(order, staff, request.getNote());
    releaseOrderBoxes(order);

    log.info("Order {} checked out successfully", orderId);
    return paymentMapper.toResponse(payment);
  }

  // ===== Collect Order =====

  @Transactional
  public OrderResponse collectOrder(Long orderId, Long staffId) {
    log.info("Collecting order: {} by staff: {}", orderId, staffId);

    Order order = findOrderById(orderId);
    validateOrderStatus(order.getStatus(), List.of(OrderStatus.WAITING), "E_ORDER003");

    User staff = findUserById(staffId);

    order.setStatus(OrderStatus.COLLECTED);
    order.setStaff(staff);
    releaseBox(order.getSendBox());
    // Also release multiple send boxes if used
    if (order.getSendBoxes() != null && !order.getSendBoxes().isEmpty()) {
      order.getSendBoxes().forEach(this::releaseBox);
      order.getSendBoxes().clear();
    }

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} collected by staff {}", orderId, staffId);

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Update Order Weight (Staff after collection) =====

  @Transactional
  public OrderResponse updateOrderWeight(
      Long orderId, UpdateOrderWeightRequest request, Long staffId) {
    log.info(
        "Updating order {} weight: {} {} by staff {}",
        orderId,
        request.getActualWeight(),
        request.getWeightUnit(),
        staffId);

    Order order = findOrderById(orderId);

    // Validate status - can only update weight after COLLECTED
    validateOrderStatus(
        order.getStatus(), List.of(OrderStatus.COLLECTED, OrderStatus.PROCESSING), "E_ORDER011");

    User staff = findUserById(staffId);

    // Update weight info
    order.setActualWeight(request.getActualWeight());
    order.setWeightUnit(request.getWeightUnit());
    order.setStaff(staff);
    if (request.getStaffNote() != null) {
      order.setStaffNote(request.getStaffNote());
    }

    // Update order items if provided
    if (request.getItems() != null && !request.getItems().isEmpty()) {
      // Clear existing and add new
      order.getOrderDetails().clear();
      addOrderDetails(order, request.getItems());
    }

    Order savedOrder = orderRepository.save(order);
    log.info(
        "Order {} weight updated to {} {}",
        orderId,
        request.getActualWeight(),
        request.getWeightUnit());

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Return Order =====

  @Transactional
  public OrderResponse returnOrder(Long orderId, Long boxId, Long staffId) {
    log.info("Returning order: {} to box: {} by staff: {}", orderId, boxId, staffId);

    Order order = findOrderById(orderId);
    validateOrderStatus(order.getStatus(), List.of(OrderStatus.READY), "E_ORDER004");

    Box receiveBox = findBoxById(boxId);
    validateBoxAvailable(receiveBox);

    User staff = findUserById(staffId);

    OrderStatus oldStatus = order.getStatus();

    LocalDateTime now = LocalDateTime.now();
    order.setStatus(OrderStatus.RETURNED);
    order.setReceiveBox(receiveBox);
    order.setStaff(staff);
    order.setPinCode(generatePinCode());
    order.setPinCodeIssuedAt(now);

    // Set returnedAt and pickupDeadline for overtime calculation
    order.setReturnedAt(now);
    order.setPickupDeadline(now.plusHours(pickupHoursLimit));

    receiveBox.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(receiveBox);

    Order savedOrder = orderRepository.save(order);
    log.info(
        "Order {} returned to box {}. Pickup deadline: {}",
        orderId,
        boxId,
        order.getPickupDeadline());

    // Send notification
    notificationService.sendOrderStatusNotification(savedOrder, oldStatus, OrderStatus.RETURNED);

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Cancel Order =====

  @Transactional
  public OrderResponse cancelOrder(Long orderId, Integer reason) {
    log.info("Canceling order: {} with reason: {}", orderId, reason);

    Order order = findOrderById(orderId);
    validateOrderStatus(order.getStatus(), CANCEL_VALID_STATUSES, "E_ORDER005");

    OrderStatus oldStatus = order.getStatus();

    releaseOrderBoxes(order);

    order.setStatus(OrderStatus.CANCELED);
    order.setCancelReason(reason);

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} canceled", orderId);

    // Send notification
    notificationService.sendOrderStatusNotification(savedOrder, oldStatus, OrderStatus.CANCELED);

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Query Methods =====

  @Transactional(readOnly = true)
  public Page<OrderResponse> getOrders(Pageable pageable) {
    return orderRepository.findByDeleteFlagFalse(pageable).map(orderMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderById(Long orderId) {
    return orderMapper.toResponse(findOrderById(orderId));
  }

  @Transactional(readOnly = true)
  public OrderStatusResponse getOrderStatus(Long orderId, Long userId) {
    log.info("Getting order status for order: {} by user: {}", orderId, userId);
    Order order = findOrderById(orderId);

    // Verify user owns this order
    if (!order.getSender().getId().equals(userId)) {
      throw new OrderException("E_ORDER_NOT_OWNER");
    }

    return buildOrderStatusResponse(order);
  }

  private OrderStatusResponse buildOrderStatusResponse(Order order) {
    // Check if paid
    List<Payment> payments = paymentRepository.findByOrderId(order.getId());
    BigDecimal totalPaid =
        payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    boolean isPaid = totalPaid.compareTo(order.getTotalPrice()) >= 0;

    // Get box number based on status
    Integer boxNumber = null;
    if (order.getReceiveBox() != null) {
      boxNumber = order.getReceiveBox().getBoxNumber();
    } else if (order.getSendBox() != null) {
      boxNumber = order.getSendBox().getBoxNumber();
    }

    return OrderStatusResponse.builder()
        .orderId(order.getId())
        .status(order.getStatus())
        .statusDescription(getStatusDescription(order.getStatus()))
        .pinCode(order.getPinCode())
        .lockerName(order.getLocker() != null ? order.getLocker().getName() : null)
        .lockerCode(order.getLocker() != null ? order.getLocker().getCode() : null)
        .boxNumber(boxNumber)
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .estimatedReadyAt(order.getIntendedReceiveAt())
        .completedAt(order.getCompletedAt())
        .isPaid(isPaid)
        .nextAction(getNextAction(order.getStatus(), isPaid))
        .build();
  }

  private String getStatusDescription(OrderStatus status) {
    return switch (status) {
      case INITIALIZED -> "Đơn hàng mới tạo, chờ bạn bỏ đồ vào tủ";
      case RESERVED -> "Đã đặt chỗ, chờ xác nhận";
      case WAITING -> "Đã bỏ đồ, chờ nhân viên thu gom";
      case COLLECTED -> "Nhân viên đã lấy đồ, đang vận chuyển";
      case PROCESSING -> "Đồ đang được giặt/xử lý";
      case READY -> "Đồ đã giặt xong, chờ trả vào tủ";
      case RETURNED -> "Đồ đã trả vào tủ, sẵn sàng lấy";
      case COMPLETED -> "Đơn hàng hoàn thành";
      case CANCELED -> "Đơn hàng đã hủy";
    };
  }

  private String getNextAction(OrderStatus status, boolean isPaid) {
    return switch (status) {
      case INITIALIZED -> "Mang đồ đến tủ và nhập mã PIN để mở tủ, bỏ đồ vào";
      case RESERVED -> "Xác nhận đơn hàng";
      case WAITING -> "Chờ nhân viên đến lấy đồ";
      case COLLECTED, PROCESSING -> "Chờ đồ được xử lý";
      case READY -> "Chờ nhân viên trả đồ vào tủ";
      case RETURNED -> isPaid ? "Đến tủ, nhập mã PIN để lấy đồ" : "Thanh toán để lấy đồ";
      case COMPLETED -> "Đánh giá dịch vụ";
      case CANCELED -> "Tạo đơn hàng mới";
    };
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderByPinCode(String pinCode) {
    Order order =
        orderRepository.findByPinCode(pinCode).orElseThrow(() -> new OrderException("E_ORDER001"));
    return orderMapper.toResponse(order);
  }

  /**
   * Get order by order code (e.g., ORD-20260202-ABC123).
   *
   * @param orderCode The unique order code
   * @return OrderResponse
   * @throws OrderException if order not found
   */
  @Transactional(readOnly = true)
  public OrderResponse getOrderByCode(String orderCode) {
    log.info("Getting order by code: {}", orderCode);
    Order order =
        orderRepository
            .findByOrderCode(orderCode)
            .orElseThrow(() -> new OrderException("E_ORDER001"));
    return orderMapper.toResponse(order);
  }

  // ===== Get My Orders - Customer's own orders =====

  @Transactional(readOnly = true)
  public Page<OrderResponse> getMyOrders(Long userId, OrderStatus status, Pageable pageable) {
    log.info("Getting orders for user: {}, status: {}", userId, status);
    if (status != null) {
      return orderRepository
          .findBySenderIdAndStatusAndDeleteFlagFalse(userId, status, pageable)
          .map(orderMapper::toResponse);
    }
    return orderRepository
        .findBySenderIdAndDeleteFlagFalse(userId, pageable)
        .map(orderMapper::toResponse);
  }

  // ===== Complete Order - Customer confirms pickup =====

  @Transactional
  public OrderResponse completeOrderByCustomer(Long orderId, Long userId) {
    log.info("Completing order: {} by customer: {}", orderId, userId);

    Order order = findOrderById(orderId);

    // Validate order belongs to user
    if (!order.getSender().getId().equals(userId)) {
      throw new OrderException("E_ORDER009"); // Order does not belong to user
    }

    // Validate status - must be RETURNED (after staff returned items)
    validateOrderStatus(order.getStatus(), List.of(OrderStatus.RETURNED), "E_ORDER010");

    OrderStatus oldStatus = order.getStatus();

    // Calculate overtime fee if customer picks up late
    BigDecimal overtimeFee = calculatePickupOvertimeFee(order);
    if (overtimeFee.compareTo(BigDecimal.ZERO) > 0) {
      log.info("Order {} has overtime fee: {}", orderId, overtimeFee);
      // Add overtime fee to extra fee
      BigDecimal currentExtraFee =
          order.getExtraFee() != null ? order.getExtraFee() : BigDecimal.ZERO;
      order.setExtraFee(currentExtraFee.add(overtimeFee));

      // Recalculate total price
      BigDecimal currentTotal =
          order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
      order.setTotalPrice(currentTotal.add(overtimeFee));
    }

    order.setStatus(OrderStatus.COMPLETED);
    order.setCompletedAt(LocalDateTime.now());
    order.setPinCode(null); // Clear PIN after pickup

    // Release receive box
    releaseBox(order.getReceiveBox());

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} completed by customer", orderId);

    // Send notification
    notificationService.sendOrderStatusNotification(savedOrder, oldStatus, OrderStatus.COMPLETED);

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Reset Order PIN =====

  @Transactional
  public OrderResponse resetOrderPin(Long orderId, Long userId) {
    log.info("Resetting PIN for order: {} by user: {}", orderId, userId);

    Order order = findOrderById(orderId);

    // Validate order belongs to user
    if (!order.getSender().getId().equals(userId)) {
      throw new OrderException("E_ORDER009"); // Order does not belong to user
    }

    // Validate status: Only allow resetting PIN if the order is INITIALIZED (user
    // needs to open box
    // to drop off)
    // or RETURNED (user needs to open box to pick up)
    validateOrderStatus(
        order.getStatus(), List.of(OrderStatus.INITIALIZED, OrderStatus.RETURNED), "E_ORDER010");

    order.setPinCode(generatePinCode());
    order.setPinCodeIssuedAt(LocalDateTime.now());

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} PIN reset successfully", orderId);

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Reorder - Clone from existing completed/canceled order =====

  /**
   * Create a new order based on an existing completed or canceled order. Clones: type, lockerId,
   * serviceCategory, serviceIds, customerNote.
   */
  @Transactional
  public OrderResponse reorderFromExisting(Long originalOrderId, Long userId) {
    log.info("Reordering from order: {} by user: {}", originalOrderId, userId);

    Order originalOrder = findOrderById(originalOrderId);

    // Validate order belongs to user
    if (!originalOrder.getSender().getId().equals(userId)) {
      throw new OrderException("E_ORDER009");
    }

    // Only allow reorder from terminal statuses
    if (originalOrder.getStatus() != OrderStatus.COMPLETED
        && originalOrder.getStatus() != OrderStatus.CANCELED) {
      throw new OrderException("E_ORDER002");
    }

    // Build CreateOrderRequest from original order
    List<Long> serviceIds =
        originalOrder.getOrderDetails().stream()
            .filter(d -> d.getService() != null)
            .map(d -> d.getService().getId())
            .toList();

    CreateOrderRequest request =
        CreateOrderRequest.builder()
            .type(originalOrder.getType())
            .lockerId(originalOrder.getLocker().getId())
            .serviceCategory(originalOrder.getServiceCategory())
            .serviceIds(serviceIds.isEmpty() ? null : serviceIds)
            .customerNote(originalOrder.getCustomerNote())
            .build();

    OrderResponse newOrder = createOrder(request, userId);
    log.info("Reorder created: {} from original: {}", newOrder.getId(), originalOrderId);
    return newOrder;
  }

  @Transactional
  public OrderResponse pickupStorageOrder(Long orderId, Long userId) {
    log.info("Pickup STORAGE order: {} by customer: {}", orderId, userId);

    Order order = findOrderById(orderId);

    // Validate order belongs to user
    if (!order.getSender().getId().equals(userId)) {
      throw new OrderException("E_ORDER009"); // Order does not belong to user
    }

    // Validate order is STORAGE type
    if (order.getType() != com.huynqb.laundrylockerbackend.module.order.enums.OrderType.STORAGE) {
      throw new OrderException("E_ORDER002"); // Generic invalid action or new error code
    }

    // Validate status - must be WAITING (items in locker, waiting for collection)
    validateOrderStatus(order.getStatus(), List.of(OrderStatus.WAITING), "E_ORDER010");

    // ---- OVERTIME PENALTY LOGIC ----
    if (order.getIntendedReceiveAt() != null) {
      LocalDateTime deadlineWithGrace = order.getIntendedReceiveAt().plusMinutes(15);
      if (LocalDateTime.now().isAfter(deadlineWithGrace)) {
        long minutesOverdue =
            java.time.Duration.between(order.getIntendedReceiveAt(), LocalDateTime.now())
                .toMinutes();
        long hoursOverdue = (long) Math.ceil(minutesOverdue / 60.0);
        if (hoursOverdue < 1) hoursOverdue = 1;

        BigDecimal penaltyFee =
            BigDecimal.valueOf(10000L)
                .multiply(BigDecimal.valueOf(hoursOverdue)); // 10k VND per hour

        if (penaltyFee.compareTo(order.getExtraFee()) > 0) {
          order.setExtraFee(penaltyFee);
          BigDecimal newTotal =
              order
                  .getStoragePrice()
                  .add(order.getReservationFee())
                  .add(order.getShippingFee())
                  .add(penaltyFee)
                  .subtract(order.getDiscount());
          order.setTotalPrice(newTotal);
          orderRepository.save(order);
        }
      }
    }

    // Verify payment is complete (including any penalty)
    List<Payment> payments = paymentRepository.findByOrderId(order.getId());
    BigDecimal totalPaid =
        payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalPaid.compareTo(order.getTotalPrice()) < 0) {
      throw new OrderException(
          "E_ORDER_OVERTIME", "Đã lố giờ gửi, vui lòng thanh toán phụ phí mở tủ");
    }
    // ---- END OVERTIME PENALTY LOGIC ----

    Box boxToUnlock = order.getSendBox();
    if (boxToUnlock == null && order.getSendBoxes() != null && !order.getSendBoxes().isEmpty()) {
      boxToUnlock = order.getSendBoxes().iterator().next();
    }

    if (boxToUnlock != null) {
      // Publish MQTT unlock command to ESP8266
      try {
        String deviceId = boxToUnlock.getLocker().getCode(); // Use locker code as device ID
        lockerMqttService.sendUnlockCommand(deviceId, boxToUnlock.getBoxNumber());
      } catch (Exception e) {
        log.error(
            "Failed to send MQTT unlock command for box {}: {}",
            boxToUnlock.getId(),
            e.getMessage());
        // Don't fail the unlock response - MQTT is best-effort
      }
      releaseBox(boxToUnlock);
    }

    OrderStatus oldStatus = order.getStatus();
    order.setStatus(OrderStatus.COMPLETED);
    order.setCompletedAt(LocalDateTime.now());
    order.setPinCode(null);

    Order savedOrder = orderRepository.save(order);
    log.info("STORAGE order {} completed by customer", orderId);

    notificationService.sendOrderStatusNotification(savedOrder, oldStatus, OrderStatus.COMPLETED);
    return orderMapper.toResponse(savedOrder);
  }

  // ===== Calculate Pickup Overtime Fee =====

  /**
   * Tính phí phạt khi khách hàng lấy đồ trễ.
   *
   * <p>Logic: - Nếu pickupDeadline = null → không có phí phạt - Nếu now <= pickupDeadline → không
   * có phí phạt - overtimeHours = số giờ vượt quá deadline - overtimeFee = overtimeHours ×
   * overtimeFeePerHour - Cap by max(maxOvertimeFee, totalPrice × maxOvertimePercent / 100)
   */
  private BigDecimal calculatePickupOvertimeFee(Order order) {
    if (order.getPickupDeadline() == null) {
      return BigDecimal.ZERO;
    }

    LocalDateTime now = LocalDateTime.now();
    if (!now.isAfter(order.getPickupDeadline())) {
      return BigDecimal.ZERO;
    }

    // Calculate overtime hours
    long overtimeHours = ChronoUnit.HOURS.between(order.getPickupDeadline(), now);
    if (overtimeHours <= 0) {
      return BigDecimal.ZERO;
    }

    // Calculate raw overtime fee
    BigDecimal rawFee = BigDecimal.valueOf(overtimeHours * overtimeFeePerHour);

    // Calculate max fee based on percentage of order total
    BigDecimal totalPrice = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
    BigDecimal percentMaxFee =
        totalPrice
            .multiply(BigDecimal.valueOf(maxOvertimePercent))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

    // Cap is the minimum of: absolute max fee OR percentage max fee
    BigDecimal capFee = percentMaxFee.min(BigDecimal.valueOf(maxOvertimeFee));

    // Apply cap
    BigDecimal finalFee = rawFee.min(capFee);

    log.info(
        "Overtime calculation for order {}: hours={}, rawFee={}, percentMax={}, capFee={}, finalFee={}",
        order.getId(),
        overtimeHours,
        rawFee,
        percentMaxFee,
        capFee,
        finalFee);

    return finalFee;
  }

  // ===== Confirm Order - Customer confirms items placed =====

  @Transactional
  public OrderResponse confirmOrder(Long orderId) {
    log.info("Confirming order: {}", orderId);

    Order order = findOrderById(orderId);
    validateOrderStatus(order.getStatus(), List.of(OrderStatus.INITIALIZED), "E_ORDER006");

    OrderStatus oldStatus = order.getStatus();
    order.setStatus(OrderStatus.WAITING);

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} confirmed, now WAITING", orderId);

    // Send notification
    notificationService.sendOrderStatusNotification(savedOrder, oldStatus, OrderStatus.WAITING);

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Process Order - Staff starts processing =====

  @Transactional
  public OrderResponse processOrder(Long orderId, Long staffId) {
    log.info("Processing order: {} by staff: {}", orderId, staffId);

    Order order = findOrderById(orderId);
    validateOrderStatus(order.getStatus(), List.of(OrderStatus.COLLECTED), "E_ORDER007");

    OrderStatus oldStatus = order.getStatus();
    User staff = findUserById(staffId);
    order.setStatus(OrderStatus.PROCESSING);
    order.setStaff(staff);

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} is now PROCESSING", orderId);

    // Send notification
    notificationService.sendOrderStatusNotification(savedOrder, oldStatus, OrderStatus.PROCESSING);

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Mark Order Ready - Processing complete =====

  @Transactional
  public OrderResponse markOrderReady(Long orderId, Long staffId) {
    log.info("Marking order ready: {} by staff: {}", orderId, staffId);

    Order order = findOrderById(orderId);
    validateOrderStatus(order.getStatus(), List.of(OrderStatus.PROCESSING), "E_ORDER008");

    OrderStatus oldStatus = order.getStatus();
    User staff = findUserById(staffId);
    order.setStatus(OrderStatus.READY);
    order.setStaff(staff);

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} is now READY", orderId);

    // Send notification
    notificationService.sendOrderStatusNotification(savedOrder, oldStatus, OrderStatus.READY);

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Private Helper Methods (KISS - simple, focused methods) =====

  private User findUserById(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> new OrderException("E_USER001"));
  }

  private Order findOrderById(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderException("E_ORDER001"));
  }

  private Locker findLockerById(Long lockerId) {
    return lockerRepository.findById(lockerId).orElseThrow(() -> new OrderException("E_LOCKER001"));
  }

  private Box findBoxById(Long boxId) {
    return boxRepository.findById(boxId).orElseThrow(() -> new OrderException("E_BOX001"));
  }

  private Box findAvailableBox(Long lockerId) {
    return boxRepository
        .findFirstByLockerIdAndStatusAndIsActiveTrue(lockerId, BoxStatus.AVAILABLE)
        .orElseThrow(() -> new OrderException("E_BOX002"));
  }

  private void validateBoxAvailable(Box box) {
    if (box.getStatus() != BoxStatus.AVAILABLE) {
      throw new OrderException("E_BOX003");
    }
  }

  private void validateOrderStatus(
      OrderStatus currentStatus, List<OrderStatus> validStatuses, String errorCode) {
    if (!validStatuses.contains(currentStatus)) {
      throw new OrderException(errorCode);
    }
  }

  private Order buildOrder(CreateOrderRequest request, User sender, Locker locker, Box sendBox) {
    Order.OrderBuilder builder =
        Order.builder()
            .type(request.getType())
            .status(OrderStatus.INITIALIZED)
            .sender(sender)
            .sendBox(sendBox)
            .locker(locker)
            .orderCode(generateOrderCode())
            .pinCode(generatePinCode())
            .pinCodeIssuedAt(LocalDateTime.now())
            .customerNote(request.getCustomerNote())
            .deliveryAddress(request.getDeliveryAddress())
            // ===== NEW: Service Category =====
            .serviceCategory(request.getServiceCategory())
            // ===== NEW: Receiver Info =====
            .receiverPhone(request.getReceiverPhone())
            .receiverName(request.getReceiverName())
            // ===== NEW: Intended Receive Time =====
            .intendedReceiveAt(request.getIntendedReceiveAt());

    // Set receiver if receiverId is provided
    if (request.getReceiverId() != null) {
      User receiver = findUserById(request.getReceiverId());
      builder.receiver(receiver);
    }

    return builder.build();
  }

  private void addOrderDetails(Order order, List<OrderItemRequest> items) {
    if (items == null || items.isEmpty()) {
      return;
    }

    BigDecimal totalPrice = BigDecimal.ZERO;
    for (OrderItemRequest item : items) {
      LaundryService service =
          laundryServiceRepository
              .findById(item.getServiceId())
              .orElseThrow(() -> new OrderException("E_SERVICE001"));

      BigDecimal itemPrice = service.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

      OrderDetail detail =
          OrderDetail.builder()
              .order(order)
              .service(service)
              .quantity(item.getQuantity())
              .price(itemPrice)
              .description(item.getDescription())
              .build();

      order.getOrderDetails().add(detail);
      totalPrice = totalPrice.add(itemPrice);
    }
    order.setTotalPrice(totalPrice);
  }

  /**
   * Add order details from serviceIds list (new format). Uses estimatedWeight for price calculation
   * if provided.
   */
  private void addOrderDetailsFromServiceIds(
      Order order, List<Long> serviceIds, Double estimatedWeight) {
    if (serviceIds == null || serviceIds.isEmpty()) {
      return;
    }

    // Set estimated weight on order
    if (estimatedWeight != null) {
      order.setActualWeight(BigDecimal.valueOf(estimatedWeight));
      order.setWeightUnit("kg");
    }

    BigDecimal totalPrice = BigDecimal.ZERO;
    // Default quantity: use estimatedWeight if provided, otherwise 1
    double defaultQuantity = estimatedWeight != null ? estimatedWeight : 1.0;

    for (Long serviceId : serviceIds) {
      LaundryService service =
          laundryServiceRepository
              .findById(serviceId)
              .orElseThrow(() -> new OrderException("E_SERVICE001"));

      // Calculate price based on service pricing type
      BigDecimal itemPrice;
      double quantity;
      if ("kg".equalsIgnoreCase(service.getUnit()) && estimatedWeight != null) {
        // Per-weight service: price * estimatedWeight
        quantity = estimatedWeight;
        itemPrice = service.getPrice().multiply(BigDecimal.valueOf(estimatedWeight));
      } else {
        // Fixed price service: price * 1
        quantity = 1.0;
        itemPrice = service.getPrice();
      }

      OrderDetail detail =
          OrderDetail.builder()
              .order(order)
              .service(service)
              .quantity(quantity)
              .price(itemPrice)
              .build();

      order.getOrderDetails().add(detail);
      totalPrice = totalPrice.add(itemPrice);
    }
    order.setTotalPrice(totalPrice);
    order.setOriginalPrice(totalPrice); // Store original price before discounts
  }

  private Payment createPayment(Order order, CheckoutOrderRequest request) {
    Payment payment =
        Payment.builder()
            .order(order)
            .customer(order.getSender())
            .amount(order.getTotalPrice())
            .method(request.getPaymentMethod())
            .status(PaymentStatus.COMPLETED)
            .referenceId(UUID.randomUUID().toString())
            .content("Payment for order #" + order.getId())
            .description(request.getNote())
            .build();
    return paymentRepository.save(payment);
  }

  private void completeOrder(Order order, User staff, String note) {
    order.setStatus(OrderStatus.COMPLETED);
    order.setStaff(staff);
    order.setCompletedAt(LocalDateTime.now());
    order.setStaffNote(note);
    orderRepository.save(order);
  }

  private void releaseOrderBoxes(Order order) {
    releaseBox(order.getSendBox());
    releaseBox(order.getReceiveBox());
  }

  private void releaseBox(Box box) {
    if (box != null) {
      box.setStatus(BoxStatus.AVAILABLE);
      boxRepository.save(box);
    }
  }

  private String generatePinCode() {
    return String.format("%0" + PIN_CODE_LENGTH + "d", RANDOM.nextInt(PIN_CODE_BOUND));
  }

  /** Generate unique order code with format: ORD-YYYYMMDD-XXXXXX Example: ORD-20260202-A1B2C3 */
  private String generateOrderCode() {
    String datePart =
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    String randomPart = generateRandomAlphanumeric(6);
    return "ORD-" + datePart + "-" + randomPart;
  }

  /** Generate random alphanumeric string (uppercase letters and digits). */
  private String generateRandomAlphanumeric(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
    }
    return sb.toString();
  }

  // ===== Promotion Methods =====

  /**
   * Apply promotion to order. Validates and calculates discount.
   *
   * @param order The order to apply promotion to
   * @param promotionCode Single promotion code
   * @param promotionCodes List of promotion codes (for stackable)
   */
  private void applyPromotionToOrder(
      Order order, String promotionCode, List<String> promotionCodes) {
    List<String> codesToApply = new ArrayList<>();

    // Collect codes to apply
    if (promotionCode != null && !promotionCode.isBlank()) {
      codesToApply.add(promotionCode.toUpperCase());
    }
    if (promotionCodes != null && !promotionCodes.isEmpty()) {
      promotionCodes.stream()
          .filter(c -> c != null && !c.isBlank())
          .map(String::toUpperCase)
          .forEach(codesToApply::add);
    }

    if (codesToApply.isEmpty()) {
      return;
    }

    BigDecimal orderTotal = order.getTotalPrice();
    BigDecimal totalDiscount = BigDecimal.ZERO;
    List<String> appliedCodes = new ArrayList<>();

    for (String code : codesToApply) {
      Promotion promotion;
      PromotionUsage rewardUsage = null;

      Optional<Promotion> optPromotion = promotionRepository.findByCode(code);
      if (optPromotion.isPresent()) {
        promotion = optPromotion.get();

        // Validate normal promotion is active
        if (!promotion.isCurrentlyActive()) {
          log.warn("Promotion {} is not active: {}", code, promotion.getStatus());
          continue;
        }
      } else {
        Optional<PromotionUsage> optUsage = promotionUsageRepository.findByRewardCode(code);
        if (optUsage.isEmpty()) {
          log.warn("Promotion code not found: {}", code);
          continue;
        }

        rewardUsage = optUsage.get();
        promotion = rewardUsage.getPromotion();

        // Reward code must belong to the order owner and still be valid.
        if (!rewardUsage.getUser().getId().equals(order.getSender().getId())) {
          log.warn(
              "Reward code {} does not belong to order owner {}", code, order.getSender().getId());
          continue;
        }
        if (!rewardUsage.isValid()) {
          log.warn("Reward code {} is not valid (status={})", code, rewardUsage.getStatus());
          continue;
        }
      }

      // Validate minimum order amount
      if (promotion.getMinOrderAmount() != null
          && orderTotal.compareTo(promotion.getMinOrderAmount()) < 0) {
        log.warn(
            "Order total {} is less than minimum required {} for promotion {}",
            orderTotal,
            promotion.getMinOrderAmount(),
            code);
        continue;
      }

      // Check if stackable (only first code or stackable codes)
      if (!appliedCodes.isEmpty() && !promotion.getStackable()) {
        log.warn("Promotion {} is not stackable, skipping", code);
        continue;
      }

      // Calculate discount
      BigDecimal discount = calculatePromotionDiscount(promotion, orderTotal);
      totalDiscount = totalDiscount.add(discount);
      appliedCodes.add(code);

      log.info("Applied promotion {}: discount = {}", code, discount);

      if (rewardUsage != null) {
        rewardUsage.markAsUsed(order);
        rewardUsage.setDiscountApplied(discount);
        promotionUsageRepository.save(rewardUsage);
      } else {
        promotionRepository.incrementUsageCount(promotion.getId());
      }
    }

    if (!appliedCodes.isEmpty()) {
      // Save original price before discount
      order.setOriginalPrice(orderTotal);

      // Set promotion codes
      order.setPromotionCode(appliedCodes.get(0));
      order.setAppliedPromotionCodes(String.join(",", appliedCodes));

      // Apply discount
      order.setDiscount(totalDiscount);
      order.setTotalPrice(orderTotal.subtract(totalDiscount).max(BigDecimal.ZERO));

      log.info(
          "Order total updated: original={}, discount={}, final={}",
          orderTotal,
          totalDiscount,
          order.getTotalPrice());
    }
  }

  /**
   * Calculate discount amount for a promotion.
   *
   * @param promotion The promotion to calculate
   * @param orderTotal The order total before discount
   * @return The calculated discount amount
   */
  private BigDecimal calculatePromotionDiscount(Promotion promotion, BigDecimal orderTotal) {
    BigDecimal discount;

    switch (promotion.getDiscountType()) {
      case PERCENTAGE:
        // Calculate percentage discount
        discount =
            orderTotal
                .multiply(promotion.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        // Apply max discount cap if exists
        if (promotion.getMaxDiscountAmount() != null
            && discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
          discount = promotion.getMaxDiscountAmount();
        }
        break;

      case FIXED_AMOUNT:
        // Fixed amount discount
        discount = promotion.getDiscountValue();
        break;

      case FREE_SERVICE:
        // Free service - use the service price as discount
        // TODO: Implement service-specific free discount
        discount = BigDecimal.ZERO;
        break;

      default:
        discount = BigDecimal.ZERO;
    }

    // Ensure discount doesn't exceed order total
    return discount.min(orderTotal);
  }

  // ===== Apply Promotion to Existing Order =====

  /**
   * Apply promotion code to an existing order.
   *
   * @param orderId The order ID
   * @param promotionCode The promotion code to apply
   * @return Updated order response
   */
  @Transactional
  public OrderResponse applyPromotionCode(Long orderId, String promotionCode) {
    log.info("Applying promotion {} to order {}", promotionCode, orderId);

    Order order = findOrderById(orderId);

    // Only allow applying promotion before payment
    if (order.getStatus() != OrderStatus.INITIALIZED
        && order.getStatus() != OrderStatus.WAITING
        && order.getStatus() != OrderStatus.RETURNED) {
      throw new OrderException("E_ORDER011"); // Cannot apply promotion at this stage
    }

    // Reset previous discount if any
    if (order.getOriginalPrice() != null
        && order.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
      order.setTotalPrice(order.getOriginalPrice());
    }
    order.setDiscount(BigDecimal.ZERO);
    order.setPromotionCode(null);
    order.setAppliedPromotionCodes(null);

    // Apply new promotion
    applyPromotionToOrder(order, promotionCode, null);

    Order savedOrder = orderRepository.save(order);
    return orderMapper.toResponse(savedOrder);
  }

  // ===== Remove Promotion from Order =====

  /**
   * Remove promotion from an order.
   *
   * @param orderId The order ID
   * @return Updated order response
   */
  @Transactional
  public OrderResponse removePromotion(Long orderId) {
    log.info("Removing promotion from order {}", orderId);

    Order order = findOrderById(orderId);

    // Only allow removing promotion before payment
    if (order.getStatus() != OrderStatus.INITIALIZED
        && order.getStatus() != OrderStatus.WAITING
        && order.getStatus() != OrderStatus.RETURNED) {
      throw new OrderException("E_ORDER012"); // Cannot remove promotion at this stage
    }

    // Restore original price
    if (order.getOriginalPrice() != null
        && order.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
      order.setTotalPrice(order.getOriginalPrice());
    }

    order.setDiscount(BigDecimal.ZERO);
    order.setPromotionCode(null);
    order.setAppliedPromotionCodes(null);
    order.setOriginalPrice(null);

    Order savedOrder = orderRepository.save(order);
    return orderMapper.toResponse(savedOrder);
  }
}
