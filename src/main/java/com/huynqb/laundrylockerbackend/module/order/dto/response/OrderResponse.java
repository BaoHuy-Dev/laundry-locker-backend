package com.huynqb.laundrylockerbackend.module.order.dto.response;

import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for order. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

  private Long id;
  private OrderType type;
  private OrderStatus status;
  private String pinCode;

  // Sender info
  private Long senderId;
  private String senderName;
  private String senderPhone;

  // Receiver info
  private Long receiverId;
  private String receiverName;

  // Locker info
  private Long lockerId;
  private String lockerName;
  private String lockerCode;
  private Integer sendBoxNumber;
  private Integer receiveBoxNumber;

  // Staff info
  private Long staffId;
  private String staffName;

  // Pricing
  private BigDecimal extraFee;
  private BigDecimal discount;
  private BigDecimal reservationFee;
  private BigDecimal storagePrice;
  private BigDecimal shippingFee;
  private BigDecimal totalPrice;

  // Notes
  private String description;
  private String customerNote;
  private String staffNote;
  private String deliveryAddress;

  // Timestamps
  private LocalDateTime intendedReceiveAt;
  private LocalDateTime receiveAt;
  private LocalDateTime completedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Order details
  private List<OrderDetailResponse> orderDetails;
}
