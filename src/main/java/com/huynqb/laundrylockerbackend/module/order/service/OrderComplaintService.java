package com.huynqb.laundrylockerbackend.module.order.service;

import com.huynqb.laundrylockerbackend.module.order.dto.request.OrderComplaintRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderComplaintResponse;
import com.huynqb.laundrylockerbackend.module.order.entity.OrderComplaint;
import com.huynqb.laundrylockerbackend.module.order.enums.ComplaintStatus;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.exception.OrderException;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderComplaintRepository;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing order complaints. */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderComplaintService {

  private final OrderComplaintRepository complaintRepository;
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;

  /** Create a new complaint for a completed order. */
  @Transactional
  public OrderComplaintResponse createComplaint(
      Long orderId, OrderComplaintRequest request, Long userId) {

    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new OrderException("Order not found with id: " + orderId));

    // Only allow complaints for completed orders
    if (order.getStatus() != OrderStatus.COMPLETED) {
      throw new OrderException("Can only create complaints for completed orders");
    }

    // Verify user is the order owner
    if (!order.getSender().getId().equals(userId)) {
      throw new OrderException("You can only create complaints for your own orders");
    }

    // Check if complaint already exists
    if (complaintRepository.existsByOrderIdAndUserId(orderId, userId)) {
      throw new OrderException("A complaint already exists for this order");
    }

    User user =
        userRepository.findById(userId).orElseThrow(() -> new OrderException("User not found"));

    // Convert image URLs list to comma-separated string
    String imageUrlsStr = null;
    if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
      imageUrlsStr = String.join(",", request.getImageUrls());
    }

    OrderComplaint complaint =
        OrderComplaint.builder()
            .order(order)
            .user(user)
            .type(request.getType())
            .description(request.getDescription())
            .imageUrls(imageUrlsStr)
            .status(ComplaintStatus.PENDING)
            .build();

    complaint = complaintRepository.save(complaint);
    log.info("Created complaint {} for order {} by user {}", complaint.getId(), orderId, userId);

    return mapToResponse(complaint);
  }

  /** Get all complaints for a specific order. */
  public List<OrderComplaintResponse> getOrderComplaints(Long orderId) {
    List<OrderComplaint> complaints =
        complaintRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    return complaints.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  /** Get all complaints by a specific user. */
  public List<OrderComplaintResponse> getMyComplaints(Long userId) {
    List<OrderComplaint> complaints = complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    return complaints.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  /** Map entity to response DTO. */
  private OrderComplaintResponse mapToResponse(OrderComplaint complaint) {
    List<String> imageUrls = Collections.emptyList();
    if (complaint.getImageUrls() != null && !complaint.getImageUrls().isEmpty()) {
      imageUrls = Arrays.asList(complaint.getImageUrls().split(","));
    }

    return OrderComplaintResponse.builder()
        .id(complaint.getId())
        .orderId(complaint.getOrder().getId())
        .orderCode(complaint.getOrder().getOrderCode())
        .userId(complaint.getUser().getId())
        .userName(
            (complaint.getUser().getFirstName() != null ? complaint.getUser().getFirstName() : "")
                + " "
                + (complaint.getUser().getLastName() != null
                    ? complaint.getUser().getLastName()
                    : ""))
        .type(complaint.getType().name())
        .description(complaint.getDescription())
        .imageUrls(imageUrls)
        .status(complaint.getStatus().name())
        .resolution(complaint.getResolution())
        .createdAt(complaint.getCreatedAt())
        .resolvedAt(complaint.getResolvedAt())
        .build();
  }
}
