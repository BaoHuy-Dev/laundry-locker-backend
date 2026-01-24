package com.huynqb.laundrylockerbackend.module.loyalty.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampTransactionType;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.user.model.User;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Transaction history for stamps earned/redeemed. */
@Entity
@Table(name = "stamp_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StampTransaction extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stamp_card_id", nullable = false)
  private StampCard stampCard;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StampTransactionType type;

  /** Number of stamps (positive for earn, negative for redeem). */
  @Column(nullable = false)
  private Integer stamps;

  /** Stamps balance after this transaction. */
  @Column(nullable = false)
  private Integer stampsAfter;

  /** Rewards balance after this transaction. */
  @Column(nullable = false)
  private Integer rewardsAfter;

  /** Discount amount applied if reward redeemed. */
  @Column(precision = 12, scale = 2)
  private BigDecimal discountApplied;

  @Column(length = 500)
  private String description;
}
