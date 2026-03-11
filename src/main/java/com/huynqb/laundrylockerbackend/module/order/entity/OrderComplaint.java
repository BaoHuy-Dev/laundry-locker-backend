package com.huynqb.laundrylockerbackend.module.order.entity;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.order.enums.ComplaintStatus;
import com.huynqb.laundrylockerbackend.module.order.enums.ComplaintType;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** Entity representing a customer complaint about an order. */
@Entity
@Table(name = "order_complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderComplaint extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ComplaintType type;

  @Column(nullable = false, length = 1000)
  private String description;

  /** Comma-separated list of image URLs. */
  @Column(length = 2000)
  private String imageUrls;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ComplaintStatus status = ComplaintStatus.PENDING;

  /** Admin/partner resolution message. */
  @Column(length = 1000)
  private String resolution;

  private LocalDateTime resolvedAt;
}
