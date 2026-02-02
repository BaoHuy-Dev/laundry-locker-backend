package com.huynqb.laundrylockerbackend.module.admin.entity;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity for promotions/discount codes. */
@Entity
@Table(
    name = "promotions",
    indexes = {
      @Index(name = "idx_promotion_code", columnList = "code", unique = true),
      @Index(name = "idx_promotion_dates", columnList = "start_date, end_date"),
      @Index(name = "idx_promotion_active", columnList = "is_active")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code", nullable = false, length = 20, unique = true)
  private String code;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "description", length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type", nullable = false, length = 20)
  private DiscountType discountType;

  @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
  private BigDecimal discountValue;

  @Column(name = "max_discount_amount", precision = 12, scale = 2)
  private BigDecimal maxDiscountAmount;

  @Column(name = "min_order_amount", precision = 12, scale = 2)
  private BigDecimal minOrderAmount;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDateTime endDate;

  @Column(name = "total_usage_limit")
  private Integer totalUsageLimit;

  @Column(name = "current_usage_count")
  @Builder.Default
  private Integer currentUsageCount = 0;

  @Column(name = "per_user_limit")
  @Builder.Default
  private Integer perUserLimit = 1;

  @Column(name = "applicable_service_ids", columnDefinition = "TEXT")
  private String applicableServiceIds; // JSON array

  @Column(name = "applicable_store_ids", columnDefinition = "TEXT")
  private String applicableStoreIds; // JSON array

  @Column(name = "applicable_tiers", columnDefinition = "TEXT")
  private String applicableTiers; // JSON array

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "priority")
  @Builder.Default
  private Integer priority = 1;

  // ========== LOYALTY REWARD FIELDS (consolidated from loyalty_rewards table) ==========

  /** How to acquire this promotion: CODE (promo code), POINTS (loyalty points), AUTO (automatic) */
  @Enumerated(EnumType.STRING)
  @Column(name = "acquisition_type", length = 20)
  @Builder.Default
  private AcquisitionType acquisitionType = AcquisitionType.CODE;

  /** Points required to redeem (for POINTS acquisition type) */
  @Column(name = "points_required")
  private Integer pointsRequired;

  /** Type of reward: DISCOUNT, FREE_SERVICE, MERCHANDISE, VOUCHER */
  @Enumerated(EnumType.STRING)
  @Column(name = "reward_type", length = 50)
  private RewardType rewardType;

  /** Value of the reward (service ID, item description, etc.) */
  @Column(name = "reward_value", columnDefinition = "TEXT")
  private String rewardValue;

  /** Image URL for the reward */
  @Column(name = "image_url", length = 500)
  private String imageUrl;

  /** Remaining quantity (for limited rewards) */
  @Column(name = "remaining_quantity")
  private Integer remainingQuantity;

  /** Minimum loyalty tier required */
  @Column(name = "minimum_tier", length = 20)
  private String minimumTier;

  /** Category of the promotion/reward */
  @Column(name = "category", length = 50)
  private String category;

  @Column(name = "stackable")
  @Builder.Default
  private Boolean stackable = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  public enum DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT,
    FREE_SERVICE
  }

  /** How the promotion is acquired */
  public enum AcquisitionType {
    CODE, // Traditional promo code
    POINTS, // Redeemed with loyalty points
    AUTO // Automatically applied
  }

  /** Type of reward for loyalty redemptions */
  public enum RewardType {
    DISCOUNT, // Discount on order
    FREE_SERVICE, // Free laundry service
    MERCHANDISE, // Physical item
    VOUCHER // Voucher/gift card
  }

  public boolean isCurrentlyActive() {
    LocalDateTime now = LocalDateTime.now();
    return isActive
        && now.isAfter(startDate)
        && now.isBefore(endDate)
        && (totalUsageLimit == null || currentUsageCount < totalUsageLimit);
  }

  public String getStatus() {
    LocalDateTime now = LocalDateTime.now();
    if (!isActive) {
      return "INACTIVE";
    }
    if (now.isBefore(startDate)) {
      return "UPCOMING";
    }
    if (now.isAfter(endDate)) {
      return "EXPIRED";
    }
    if (totalUsageLimit != null && currentUsageCount >= totalUsageLimit) {
      return "DEPLETED";
    }
    return "ACTIVE";
  }
}
