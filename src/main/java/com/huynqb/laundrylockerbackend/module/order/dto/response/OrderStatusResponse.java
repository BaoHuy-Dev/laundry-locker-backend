package com.huynqb.laundrylockerbackend.module.order.dto.response;

import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Lightweight response DTO for order status tracking. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponse {

  private Long orderId;
  private OrderStatus status;
  private String statusDescription;
  private String pinCode;

  // Locker info for pickup
  private String lockerName;
  private String lockerCode;
  private Integer boxNumber;

  // Timestamps
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime estimatedReadyAt;
  private LocalDateTime completedAt;

  // Payment status
  private Boolean isPaid;

  // Next action hint for user
  private String nextAction;
}
