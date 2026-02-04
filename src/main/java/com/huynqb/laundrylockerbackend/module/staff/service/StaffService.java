package com.huynqb.laundrylockerbackend.module.staff.service;

import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerResponse;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.locker.service.LockerService;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.mapper.OrderMapper;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.staff.dto.request.StaffUnlockBoxRequest;
import com.huynqb.laundrylockerbackend.module.staff.dto.response.StaffOrderSummaryResponse;
import com.huynqb.laundrylockerbackend.module.staff.dto.response.StaffUnlockBoxResponse;
import com.huynqb.laundrylockerbackend.module.staff.mapper.StaffMapper;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for staff-specific operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final LockerService lockerService;
  private final BoxRepository boxRepository;
  private final OrderMapper orderMapper;
  private final StaffMapper staffMapper;

  @Value("${app.staff.master-pin:999999}")
  private String masterPin;

  /** Get orders by status for staff to work on. */
  @Transactional(readOnly = true)
  public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
    log.info("Getting orders with status: {}", status);
    return orderRepository
        .findByStatusInAndDeleteFlagFalse(List.of(status), pageable)
        .map(orderMapper::toResponse);
  }

  /** Get waiting orders (ready for collection). */
  @Transactional(readOnly = true)
  public Page<OrderResponse> getWaitingOrders(Pageable pageable) {
    return getOrdersByStatus(OrderStatus.WAITING, pageable);
  }

  /** Get processing orders. */
  @Transactional(readOnly = true)
  public Page<OrderResponse> getProcessingOrders(Pageable pageable) {
    return orderRepository
        .findByStatusInAndDeleteFlagFalse(
            List.of(OrderStatus.COLLECTED, OrderStatus.PROCESSING), pageable)
        .map(orderMapper::toResponse);
  }

  /** Get ready orders (ready to return to locker). */
  @Transactional(readOnly = true)
  public Page<OrderResponse> getReadyOrders(Pageable pageable) {
    return getOrdersByStatus(OrderStatus.READY, pageable);
  }

  /** Get orders assigned to specific staff. */
  @Transactional(readOnly = true)
  public Page<OrderResponse> getMyAssignedOrders(Long staffId, Pageable pageable) {
    log.info("Getting orders assigned to staff: {}", staffId);
    return orderRepository
        .findByStaffIdAndDeleteFlagFalse(staffId, pageable)
        .map(orderMapper::toResponse);
  }

  /** Assign an order to staff (self-assignment). */
  @Transactional
  public OrderResponse assignOrderToStaff(Long orderId, Long staffId) {
    log.info("Assigning order: {} to staff: {}", orderId, staffId);

    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

    // Check if order is in valid state for assignment
    if (order.getStatus() != OrderStatus.WAITING) {
      throw new RuntimeException("Order can only be assigned when status is WAITING");
    }

    // Check if already assigned
    if (order.getStaff() != null) {
      throw new RuntimeException("Order is already assigned to another staff");
    }

    User staff =
        userRepository
            .findById(staffId)
            .orElseThrow(() -> new RuntimeException("Staff not found: " + staffId));

    order.setStaff(staff);
    Order savedOrder = orderRepository.save(order);

    log.info("Order {} assigned to staff {}", orderId, staffId);
    return orderMapper.toResponse(savedOrder);
  }

  /** Get staff dashboard summary. */
  @Transactional(readOnly = true)
  public StaffOrderSummaryResponse getOrderSummary() {
    Pageable recentPage = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));

    long waitingCount = orderRepository.findByStatus(OrderStatus.WAITING).size();
    long processingCount = orderRepository.findByStatus(OrderStatus.PROCESSING).size();
    long collectedCount = orderRepository.findByStatus(OrderStatus.COLLECTED).size();
    long readyCount = orderRepository.findByStatus(OrderStatus.READY).size();

    List<OrderResponse> recentOrders =
        orderRepository
            .findByStatusInAndDeleteFlagFalse(
                List.of(
                    OrderStatus.WAITING,
                    OrderStatus.COLLECTED,
                    OrderStatus.PROCESSING,
                    OrderStatus.READY),
                recentPage)
            .map(orderMapper::toResponse)
            .getContent();

    return staffMapper.toOrderSummaryResponse(
        waitingCount, processingCount, collectedCount, readyCount, recentOrders);
  }

  /** Get all lockers with box availability for staff. */
  @Transactional(readOnly = true)
  public List<LockerResponse> getAllLockers() {
    log.info("Staff getting all lockers");
    return lockerService.getAllLockers();
  }

  /** Get lockers by store ID for staff. */
  @Transactional(readOnly = true)
  public List<LockerResponse> getLockersByStore(Long storeId) {
    log.info("Staff getting lockers for store: {}", storeId);
    return lockerService.getLockersByStore(storeId);
  }

  /** Unlock box using master PIN (for staff). */
  @Transactional
  public StaffUnlockBoxResponse unlockBox(StaffUnlockBoxRequest request, Long staffId) {
    log.info("Staff {} unlocking box {}", staffId, request.getBoxId());

    // Validate master PIN
    if (!masterPin.equals(request.getMasterPin())) {
      return staffMapper.toErrorUnlockResponse(request.getBoxId(), "Invalid master PIN");
    }

    // Get box
    Box box = boxRepository.findById(request.getBoxId()).orElse(null);

    if (box == null) {
      return staffMapper.toErrorUnlockResponse(request.getBoxId(), "Box not found");
    }

    // Generate unlock token
    String unlockToken = UUID.randomUUID().toString();

    log.info("Staff {} successfully unlocked box {}", staffId, box.getId());

    return staffMapper.toSuccessUnlockResponse(box, request.getOrderId(), unlockToken);
  }
}
