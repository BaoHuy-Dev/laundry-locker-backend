package com.huynqb.laundrylockerbackend.module.admin.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for tracking promotion usage and loyalty reward redemptions. Consolidated from
 * promotion_usage and loyalty_reward_redemptions tables.
 */
@Entity
@Table(
    name = "promotion_usage",
    indexes = {
      @Index(name = "idx_promotion_usage_user", columnList = "user_id"),
      @Index(name = "idx_promotion_usage_promotion", columnList = "promotion_id"),
      @Index(name = "idx_promotion_usage_status", columnList = "status"),
      @Index(name = "idx_promotion_usage_type", columnList = "usage_type")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_promotion_usage",
          columnNames = {"promotion_id", "user_id", "order_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionUsage extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "promotion_id", nullable = false)
  private Promotion promotion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order;

  @Column(name = "discount_applied", precision = 12, scale = 2)
  private BigDecimal discountApplied;

  @Column(name = "used_at", nullable = false)
  private LocalDateTime usedAt;

  // ========== REDEMPTION FIELDS (consolidated from loyalty_reward_redemptions) ==========

  /** Type of usage: PROMO_CODE or POINTS_REDEMPTION */
  @Enumerated(EnumType.STRING)
  @Column(name = "usage_type", length = 20)
  @Builder.Default
  private UsageType usageType = UsageType.PROMO_CODE;

  /** Points spent for redemption (nullable for promo codes) */
  @Column(name = "points_spent")
  private Integer pointsSpent;

  /** Generated reward code (for redemptions) */
  @Column(name = "reward_code", length = 50, unique = true)
  private String rewardCode;

  /** Status of the usage/redemption */
  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20)
  @Builder.Default
  private UsageStatus status = UsageStatus.ACTIVE;

  /** Expiration date for the redeemed reward */
  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  /** Order where the reward was actually used */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "used_order_id")
  private Order usedOrder;

  /** Type of promotion usage */
  public enum UsageType {
    PROMO_CODE, // Traditional promo code usage
    POINTS_REDEMPTION // Loyalty points redemption
  }

  /** Status of the usage */
  public enum UsageStatus {
    ACTIVE, // Available for use
    USED, // Already used
    EXPIRED, // Expired without use
    CANCELLED // Cancelled/refunded
  }

  @PrePersist
  protected void onCreate() {
    if (usedAt == null) {
      usedAt = LocalDateTime.now();
    }
  }

  /** Check if this usage is still valid for use */
  public boolean isValid() {
    if (status != UsageStatus.ACTIVE) {
      return false;
    }
    if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
      return false;
    }
    return true;
  }

  /** Mark this usage as used with the given order */
  public void markAsUsed(Order order) {
    this.usedOrder = order;
    this.status = UsageStatus.USED;
  }
}
