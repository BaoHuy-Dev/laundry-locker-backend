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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "send_box_id")
  private Box sendBox;

  // Receiver info
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id")
  private User receiver;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receive_box_id")
  private Box receiveBox;

  private LocalDateTime receiveAt;

  private LocalDateTime intendedReceiveAt;

  private LocalDateTime intendedOvertime;

  private LocalDateTime completedAt;

  // Locker and Staff
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "locker_id", nullable = false)
  private Locker locker;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "staff_id")
  private User staff;

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

  // Order details
  @Builder.Default
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderDetail> orderDetails = new ArrayList<>();
}
