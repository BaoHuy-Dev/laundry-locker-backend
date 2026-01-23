package com.huynqb.laundrylockerbackend.module.admin.service;

import com.huynqb.laundrylockerbackend.module.admin.dto.response.OrderStatisticsResponse;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.RevenueReportResponse;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.mapper.OrderMapper;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentStatus;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import com.huynqb.laundrylockerbackend.module.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for admin order and payment management. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {

  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final OrderMapper orderMapper;

  /** Get all orders with optional filters. */
  @Transactional(readOnly = true)
  public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
    log.info("Admin getting all orders, status filter: {}", status);
    if (status != null) {
      return orderRepository
          .findByStatusInAndDeleteFlagFalse(List.of(status), pageable)
          .map(orderMapper::toResponse);
    }
    return orderRepository.findByDeleteFlagFalse(pageable).map(orderMapper::toResponse);
  }

  /** Get order by ID. */
  @Transactional(readOnly = true)
  public OrderResponse getOrderById(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    return orderMapper.toResponse(order);
  }

  /** Force update order status (admin override). */
  @Transactional
  public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
    log.info("Admin force updating order {} to status {}", orderId, newStatus);

    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

    order.setStatus(newStatus);
    if (newStatus == OrderStatus.COMPLETED) {
      order.setCompletedAt(LocalDateTime.now());
    }

    Order savedOrder = orderRepository.save(order);
    log.info("Order {} status updated to {}", orderId, newStatus);

    return orderMapper.toResponse(savedOrder);
  }

  /** Get order statistics. */
  @Transactional(readOnly = true)
  public OrderStatisticsResponse getOrderStatistics() {
    log.info("Getting order statistics");

    List<Order> allOrders = orderRepository.findAll();

    long totalOrders = allOrders.size();
    long completedOrders =
        allOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
    long canceledOrders =
        allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELED).count();
    long pendingOrders =
        allOrders.stream()
            .filter(
                o ->
                    o.getStatus() != OrderStatus.COMPLETED && o.getStatus() != OrderStatus.CANCELED)
            .count();

    BigDecimal totalRevenue =
        allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
            .map(Order::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal averageOrderValue =
        completedOrders > 0
            ? totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    // Today's orders
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    long ordersToday =
        allOrders.stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfDay))
            .count();

    // This week's orders
    LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
    long ordersThisWeek =
        allOrders.stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfWeek))
            .count();

    // This month's orders
    LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    long ordersThisMonth =
        allOrders.stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfMonth))
            .count();

    return OrderStatisticsResponse.builder()
        .totalOrders(totalOrders)
        .completedOrders(completedOrders)
        .canceledOrders(canceledOrders)
        .pendingOrders(pendingOrders)
        .totalRevenue(totalRevenue)
        .averageOrderValue(averageOrderValue)
        .ordersToday(ordersToday)
        .ordersThisWeek(ordersThisWeek)
        .ordersThisMonth(ordersThisMonth)
        .build();
  }

  /** Get revenue report. */
  @Transactional(readOnly = true)
  public RevenueReportResponse getRevenueReport() {
    log.info("Getting revenue report");

    List<Payment> allPayments = paymentRepository.findAll();

    // Total revenue from successful payments
    BigDecimal totalRevenue =
        allPayments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Today's revenue
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    BigDecimal revenueToday =
        allPayments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(startOfDay))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // This week's revenue
    LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
    BigDecimal revenueThisWeek =
        allPayments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(startOfWeek))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // This month's revenue
    LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    BigDecimal revenueThisMonth =
        allPayments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(startOfMonth))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Transaction counts
    long totalTransactions = allPayments.size();
    long successfulTransactions =
        allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count();
    long failedTransactions =
        allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count();

    // Revenue by payment method
    Map<String, BigDecimal> revenueByMethod = new HashMap<>();
    allPayments.stream()
        .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
        .forEach(
            p -> {
              String method = p.getMethod() != null ? p.getMethod().name() : "UNKNOWN";
              revenueByMethod.merge(method, p.getAmount(), BigDecimal::add);
            });

    // Daily revenue for last 7 days
    List<RevenueReportResponse.DailyRevenueEntry> dailyRevenue = new ArrayList<>();
    for (int i = 6; i >= 0; i--) {
      LocalDate date = LocalDate.now().minusDays(i);
      LocalDateTime dayStart = date.atStartOfDay();
      LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

      BigDecimal dayRevenue =
          allPayments.stream()
              .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
              .filter(
                  p ->
                      p.getCreatedAt() != null
                          && p.getCreatedAt().isAfter(dayStart)
                          && p.getCreatedAt().isBefore(dayEnd))
              .map(Payment::getAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      long orderCount =
          allPayments.stream()
              .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
              .filter(
                  p ->
                      p.getCreatedAt() != null
                          && p.getCreatedAt().isAfter(dayStart)
                          && p.getCreatedAt().isBefore(dayEnd))
              .count();

      dailyRevenue.add(
          RevenueReportResponse.DailyRevenueEntry.builder()
              .date(date)
              .revenue(dayRevenue)
              .orderCount(orderCount)
              .build());
    }

    return RevenueReportResponse.builder()
        .totalRevenue(totalRevenue)
        .revenueToday(revenueToday)
        .revenueThisWeek(revenueThisWeek)
        .revenueThisMonth(revenueThisMonth)
        .totalTransactions(totalTransactions)
        .successfulTransactions(successfulTransactions)
        .failedTransactions(failedTransactions)
        .revenueByPaymentMethod(revenueByMethod)
        .dailyRevenue(dailyRevenue)
        .build();
  }
}
