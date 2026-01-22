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
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
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

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} collected by staff {}", orderId, staffId);

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
  public OrderResponse getOrderByPinCode(String pinCode) {
    Order order =
        orderRepository.findByPinCode(pinCode).orElseThrow(() -> new OrderException("E_ORDER001"));
    return orderMapper.toResponse(order);
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
