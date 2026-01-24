package com.huynqb.laundrylockerbackend.module.loyalty.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.laundry.model.LaundryService;
import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampType;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Stamp card for tracking usage frequency. Use 6 times = get 1 free. Can be for Box usage or
 * Service usage.
 */
@Entity
@Table(name = "stamp_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StampCard extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StampType stampType;

  /** Reference to specific service (if type is SERVICE). */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_id")
  private LaundryService service;

  /**
   * Reference to box size category (if type is BOX). Using box size as reference (SMALL, MEDIUM,
   * LARGE).
   */
  private String boxSize;

  /** Number of stamps required for 1 free redemption. */
  @Builder.Default
  @Column(nullable = false)
  private Integer stampsRequired = 6;

  /** Current stamps collected. */
  @Builder.Default
  @Column(nullable = false)
  private Integer currentStamps = 0;

  /** Free rewards available to use. */
  @Builder.Default
  @Column(nullable = false)
  private Integer freeRewardsAvailable = 0;

  /** Total stamps earned all time. */
  @Builder.Default
  @Column(nullable = false)
  private Integer totalStampsEarned = 0;

  /** Total free rewards redeemed. */
  @Builder.Default
  @Column(nullable = false)
  private Integer totalRewardsRedeemed = 0;

  /**
   * Add a stamp. If reaches required, convert to free reward.
   *
   * @return true if a new reward was earned
   */
  public boolean addStamp() {
    this.currentStamps++;
    this.totalStampsEarned++;

    if (this.currentStamps >= this.stampsRequired) {
      this.currentStamps -= this.stampsRequired;
      this.freeRewardsAvailable++;
      return true;
    }
    return false;
  }

  /**
   * Redeem a free reward.
   *
   * @return true if successful, false if no rewards available
   */
  public boolean redeemReward() {
    if (this.freeRewardsAvailable <= 0) {
      return false;
    }
    this.freeRewardsAvailable--;
    this.totalRewardsRedeemed++;
    return true;
  }

  /** Check if user has free reward available. */
  public boolean hasRewardAvailable() {
    return this.freeRewardsAvailable > 0;
  }
}
