package com.huynqb.laundrylockerbackend.module.order.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderType;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Order entity representing a laundry order. */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Unique order code for display and reference (e.g., ORD-20260202-ABC123). */
  @Column(name = "order_code", unique = true, nullable = false, length = 30)
  private String orderCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderType type;

  private String pinCode;

  private LocalDateTime pinCodeIssuedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  // Sender info
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  // Single send box (for backward compatibility)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "send_box_id")
  private Box sendBox;

  // Multiple send boxes support
  @Builder.Default
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "order_send_boxes",
      joinColumns = @JoinColumn(name = "order_id"),
      inverseJoinColumns = @JoinColumn(name = "box_id"))
  private Set<Box> sendBoxes = new HashSet<>();

  // Receiver info
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id")
  private User receiver;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receive_box_id")
  private Box receiveBox;

  // Multiple receive boxes support
  @Builder.Default
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "order_receive_boxes",
      joinColumns = @JoinColumn(name = "order_id"),
      inverseJoinColumns = @JoinColumn(name = "box_id"))
  private Set<Box> receiveBoxes = new HashSet<>();

  private LocalDateTime receiveAt;

  private LocalDateTime intendedReceiveAt;

  private LocalDateTime intendedOvertime;

  private LocalDateTime completedAt;

  /** Timestamp when staff returned items to locker (status changed to RETURNED). */
  private LocalDateTime returnedAt;

  /** Deadline for customer pickup (returnedAt + pickup hours limit). */
  private LocalDateTime pickupDeadline;

  /** Phone number of receiver (if different from sender). */
  private String receiverPhone;

  /** Name of receiver (if different from sender). */
  private String receiverName;

  // Locker and Staff
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "locker_id", nullable = false)
  private Locker locker;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "staff_id")
  private User staff;

  // Actual weight (updated by staff after collection)
  @Column(precision = 10, scale = 2)
  private BigDecimal actualWeight;

  // Weight unit (kg, etc.)
  private String weightUnit;

  // Pricing
  @Builder.Default
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal extraFee = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal discount = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal reservationFee = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal storagePrice = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal shippingFee = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal totalPrice = BigDecimal.ZERO;

  // Notes and descriptions
  @Column(length = 2000)
  private String description;

  @Column(length = 1000)
  private String customerNote;

  @Column(length = 1000)
  private String staffNote;

  private Integer cancelReason;

  private String deliveryAddress;

  // ===== PROMOTION FIELDS =====

  /** Applied promotion code. */
  private String promotionCode;

  /** Applied promotion codes (comma-separated for multiple). */
  private String appliedPromotionCodes;

  /** Original price before discount. */
  @Builder.Default
  @Column(precision = 12, scale = 2)
  private BigDecimal originalPrice = BigDecimal.ZERO;

  /** Service category: STORAGE or LAUNDRY. */
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceCategory serviceCategory;

  // Order details
  @Builder.Default
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderDetail> orderDetails = new ArrayList<>();

  // Order complaints
  @Builder.Default
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderComplaint> orderComplaints = new ArrayList<>();
}
