package com.huynqb.laundrylockerbackend.module.order.dto.response;

import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceCategory;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderType;
import com.huynqb.laundrylockerbackend.module.order.enums.PricingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
  private String orderCode;
  private OrderType type;
  private OrderStatus status;
  private String pinCode;

  // ===== SERVICE CATEGORY (NEW) =====
  private ServiceCategory serviceCategory;
  private PricingType pricingType;

  // Sender info
  private Long senderId;
  private String senderName;
  private String senderPhone;

  // Receiver info
  private Long receiverId;
  private String receiverName;
  private String receiverPhone;

  // Locker info
  private Long lockerId;
  private String lockerName;
  private String lockerCode;
  private Integer sendBoxNumber;
  private Integer receiveBoxNumber;
  private Long sendBoxId;
  private Long receiveBoxId;

  // Multiple boxes support
  private Set<Integer> sendBoxNumbers;
  private Set<Integer> receiveBoxNumbers;

  // Staff info
  private Long staffId;
  private String staffName;

  // Weight info (updated by staff after collection)
  private BigDecimal actualWeight;
  private String weightUnit;

  // Pricing
  private BigDecimal extraFee;
  private BigDecimal discount;
  private BigDecimal reservationFee;
  private BigDecimal storagePrice;
  private BigDecimal shippingFee;
  private BigDecimal totalPrice;

  // ===== PROMOTION INFO (NEW) =====
  private String promotionCode;
  private List<String> appliedPromotionCodes;
  private BigDecimal originalPrice;
  private BigDecimal promotionDiscount;
  private PromotionInfoResponse promotionInfo;

  // ===== ESTIMATED PRICE (NEW - for LAUNDRY) =====
  private EstimatedPriceResponse estimatedPrice;

  // ===== PRICE BREAKDOWN (NEW) =====
  private PriceBreakdownResponse priceBreakdown;

  // ===== OVERTIME INFO (NEW) =====
  private Boolean isOvertime;
  private Integer overtimeHours;
  private LocalDateTime pickupDeadline;
  private LocalDateTime returnedAt;

  // ===== PAYMENT STATUS (NEW) =====
  private Boolean isPaid;
  private Boolean paymentRequired;

  // ===== NEXT ACTION (NEW) =====
  private String nextAction;
  private String nextActionMessage;

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
