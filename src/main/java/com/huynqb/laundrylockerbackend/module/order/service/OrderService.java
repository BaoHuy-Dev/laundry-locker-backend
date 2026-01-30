package com.huynqb.laundrylockerbackend.module.order.service;

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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final OrderMapper orderMapper;
  private final PaymentMapper paymentMapper;
  private final NotificationService notificationService;

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int PIN_CODE_LENGTH = 6;
  private static final int PIN_CODE_BOUND = 1000000;

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
    Box sendBox = findOrAssignBox(request.getBoxId(), locker.getId());

    Order order = buildOrder(request, sender, locker, sendBox);
    addOrderDetails(order, request.getItems());

    sendBox.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(sendBox);

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

    order.setStatus(OrderStatus.RETURNED);
    order.setReceiveBox(receiveBox);
    order.setStaff(staff);
    order.setPinCode(generatePinCode());
    order.setPinCodeIssuedAt(LocalDateTime.now());

    receiveBox.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(receiveBox);

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} returned to box {}", orderId, boxId);

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
    boolean isPaid = payments.stream().anyMatch(p -> p.getStatus() == PaymentStatus.COMPLETED);

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

  private Box findOrAssignBox(Long boxId, Long lockerId) {
    if (boxId != null) {
      Box box = findBoxById(boxId);
      validateBoxAvailable(box);
      return box;
    }
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
    return Order.builder()
        .type(request.getType())
        .status(OrderStatus.INITIALIZED)
        .sender(sender)
        .sendBox(sendBox)
        .locker(locker)
        .pinCode(generatePinCode())
        .pinCodeIssuedAt(LocalDateTime.now())
        .customerNote(request.getCustomerNote())
        .deliveryAddress(request.getDeliveryAddress())
        .build();
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
}
