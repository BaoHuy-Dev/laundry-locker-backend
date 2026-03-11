package com.huynqb.laundrylockerbackend.module.order.service;

import com.huynqb.laundrylockerbackend.module.order.dto.request.OrderRatingRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderRatingResponse;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderTimelineEvent;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderTimelineResponse;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.exception.OrderException;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.model.OrderRating;
import com.huynqb.laundrylockerbackend.module.order.model.OrderStatusHistory;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRatingRepository;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderStatusHistoryRepository;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for order ratings and timeline management. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderRatingService {

  private final OrderRepository orderRepository;
  private final OrderRatingRepository orderRatingRepository;
  private final OrderStatusHistoryRepository orderStatusHistoryRepository;
  private final UserRepository userRepository;

  // Status to progress percentage mapping
  private static final Map<OrderStatus, Integer> STATUS_PROGRESS =
      Map.of(
          OrderStatus.INITIALIZED, 5,
          OrderStatus.RESERVED, 10,
          OrderStatus.WAITING, 20,
          OrderStatus.COLLECTED, 35,
          OrderStatus.PROCESSING, 50,
          OrderStatus.READY, 70,
          OrderStatus.RETURNED, 85,
          OrderStatus.COMPLETED, 100);

  // Status to UI icon mapping
  private static final Map<OrderStatus, String> STATUS_ICONS =
      Map.of(
          OrderStatus.INITIALIZED, "📝",
          OrderStatus.RESERVED, "📦",
          OrderStatus.WAITING, "⏳",
          OrderStatus.COLLECTED, "🚚",
          OrderStatus.PROCESSING, "🧺",
          OrderStatus.READY, "✅",
          OrderStatus.RETURNED, "📬",
          OrderStatus.COMPLETED, "🎉");

  // ===== Rating Operations =====

  /** Create a rating for an order. */
  @Transactional
  public OrderRatingResponse createRating(Long orderId, OrderRatingRequest request, Long userId) {
    log.info("Creating rating for order: {} by user: {}", orderId, userId);

    Order order = findOrderById(orderId);
    User user = findUserById(userId);

    // Validate order belongs to user
    if (!order.getSender().getId().equals(userId)) {
      throw new OrderException("You can only rate your own orders", "ORDER_NOT_YOURS");
    }

    // Validate order is completed
    if (order.getStatus() != OrderStatus.COMPLETED) {
      throw new OrderException("Can only rate completed orders", "ORDER_NOT_COMPLETED");
    }

    // Check if already rated
    if (orderRatingRepository.existsByOrderIdAndUserId(orderId, userId)) {
      throw new OrderException("Order already rated", "ORDER_ALREADY_RATED");
    }

    // Create rating
    OrderRating rating =
        OrderRating.builder()
            .order(order)
            .user(user)
            .rating(request.getRating())
            .comment(request.getComment())
            .serviceRating(request.getServiceRating())
            .speedRating(request.getSpeedRating())
            .staffRating(request.getStaffRating())
            .build();

    rating = orderRatingRepository.save(rating);
    log.info("Created rating {} for order {}", rating.getId(), orderId);

    return mapToRatingResponse(rating);
  }

  /** Get rating for an order. */
  public OrderRatingResponse getRating(Long orderId) {
    OrderRating rating =
        orderRatingRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new OrderException("Rating not found", "RATING_NOT_FOUND"));
    return mapToRatingResponse(rating);
  }

  /** Get all ratings by a specific user. */
  public List<OrderRatingResponse> getMyRatings(Long userId) {
    return orderRatingRepository.findByUserIdAndDeleteFlagFalse(userId).stream()
        .map(this::mapToRatingResponse)
        .collect(Collectors.toList());
  }

  /** Get ratings for a store. */
  public Page<OrderRatingResponse> getStoreRatings(Long storeId, Pageable pageable) {
    return orderRatingRepository.findByStoreId(storeId, pageable).map(this::mapToRatingResponse);
  }

  /** Get average rating for a store. */
  public Double getStoreAverageRating(Long storeId) {
    return orderRatingRepository.getAverageRatingByStoreId(storeId);
  }

  /** Respond to a rating (partner only). */
  @Transactional
  public OrderRatingResponse respondToRating(Long ratingId, String response, Long partnerId) {
    OrderRating rating =
        orderRatingRepository
            .findById(ratingId)
            .orElseThrow(() -> new OrderException("Rating not found", "RATING_NOT_FOUND"));

    // Validate partner owns the order's store
    // This would require checking the order's store partner ID

    rating.setPartnerResponse(response);
    rating.setRespondedAt(LocalDateTime.now());
    rating = orderRatingRepository.save(rating);

    return mapToRatingResponse(rating);
  }

  // ===== Timeline Operations =====

  /** Get order timeline. */
  public OrderTimelineResponse getOrderTimeline(Long orderId, Long userId) {
    Order order = findOrderById(orderId);

    // Validate access
    if (!order.getSender().getId().equals(userId)) {
      throw new OrderException("Access denied", "ACCESS_DENIED");
    }

    List<OrderStatusHistory> history =
        orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId);

    List<OrderTimelineEvent> events =
        history.stream().map(this::mapToTimelineEvent).collect(Collectors.toList());

    Integer progressPercentage = STATUS_PROGRESS.getOrDefault(order.getStatus(), 0);
    if (order.getStatus() == OrderStatus.CANCELED) {
      progressPercentage = 0;
    }

    return OrderTimelineResponse.builder()
        .orderId(orderId)
        .currentStatus(order.getStatus().name())
        .progressPercentage(progressPercentage)
        .events(events)
        .build();
  }

  /** Record a status change in the timeline. */
  @Transactional
  public void recordStatusChange(
      Order order,
      OrderStatus fromStatus,
      OrderStatus toStatus,
      Long actorId,
      String actorType,
      String notes) {
    OrderStatusHistory history =
        OrderStatusHistory.builder()
            .order(order)
            .fromStatus(fromStatus != null ? fromStatus.name() : null)
            .toStatus(toStatus.name())
            .changedAt(LocalDateTime.now())
            .changedBy(actorId)
            .actorType(actorType)
            .note(notes)
            .build();

    orderStatusHistoryRepository.save(history);
    log.info("Recorded status change for order {}: {} -> {}", order.getId(), fromStatus, toStatus);
  }

  // ===== Private Helper Methods =====

  private Order findOrderById(Long orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(() -> new OrderException("Order not found", "ORDER_NOT_FOUND"));
  }

  private User findUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new OrderException("User not found", "USER_NOT_FOUND"));
  }

  private OrderRatingResponse mapToRatingResponse(OrderRating rating) {
    User user = rating.getUser();
    String userName =
        (user.getFirstName() != null ? user.getFirstName() : "")
            + " "
            + (user.getLastName() != null ? user.getLastName() : "");
    return OrderRatingResponse.builder()
        .id(rating.getId())
        .orderId(rating.getOrder().getId())
        .userId(rating.getUser().getId())
        .userName(userName.trim())
        .rating(rating.getRating())
        .comment(rating.getComment())
        .serviceRating(rating.getServiceRating())
        .speedRating(rating.getSpeedRating())
        .staffRating(rating.getStaffRating())
        .createdAt(rating.getCreatedAt())
        .partnerResponse(rating.getPartnerResponse())
        .respondedAt(rating.getRespondedAt())
        .build();
  }

  private OrderTimelineEvent mapToTimelineEvent(OrderStatusHistory history) {
    OrderStatus status = OrderStatus.valueOf(history.getToStatus());
    return OrderTimelineEvent.builder()
        .status(history.getToStatus())
        .title(getStatusTitle(status))
        .description(history.getNote() != null ? history.getNote() : getStatusDescription(status))
        .timestamp(history.getChangedAt())
        .icon(STATUS_ICONS.getOrDefault(status, "📋"))
        .color(getStatusColor(status))
        .actor(history.getActorType())
        .build();
  }

  private String getStatusTitle(OrderStatus status) {
    return switch (status) {
      case INITIALIZED -> "Order Created";
      case RESERVED -> "Locker Reserved";
      case WAITING -> "Awaiting Pickup";
      case COLLECTED -> "Items Collected";
      case PROCESSING -> "Processing";
      case READY -> "Ready for Pickup";
      case RETURNED -> "In Locker";
      case COMPLETED -> "Completed";
      case CANCELED -> "Cancelled";
    };
  }

  private String getStatusDescription(OrderStatus status) {
    return switch (status) {
      case INITIALIZED -> "Your order has been created";
      case RESERVED -> "A locker box has been reserved for you";
      case WAITING -> "Please place items in the locker";
      case COLLECTED -> "Staff has collected your items";
      case PROCESSING -> "Your laundry is being processed";
      case READY -> "Your order is ready for pickup";
      case RETURNED -> "Items have been placed in the locker";
      case COMPLETED -> "Thank you for using our service";
      case CANCELED -> "Order has been cancelled";
    };
  }

  private String getStatusColor(OrderStatus status) {
    return switch (status) {
      case INITIALIZED, RESERVED -> "#3B82F6"; // Blue
      case WAITING -> "#F59E0B"; // Amber
      case COLLECTED, PROCESSING -> "#8B5CF6"; // Purple
      case READY, RETURNED -> "#10B981"; // Green
      case COMPLETED -> "#059669"; // Dark Green
      case CANCELED -> "#EF4444"; // Red
    };
  }

  private boolean canCancelOrder(Order order) {
    return order.getStatus() == OrderStatus.INITIALIZED
        || order.getStatus() == OrderStatus.RESERVED
        || order.getStatus() == OrderStatus.WAITING;
  }
}
