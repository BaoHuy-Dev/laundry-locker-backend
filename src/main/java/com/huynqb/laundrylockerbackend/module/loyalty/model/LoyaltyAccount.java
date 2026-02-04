package com.huynqb.laundrylockerbackend.module.loyalty.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Loyalty account for tracking user points and stamps. - Points: 10,000 VND spent = 1 point earned
 * - Points can be redeemed: 10,000 points = 10,000 VND discount
 */
@Entity
@Table(name = "loyalty_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LoyaltyAccount extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  /** Current available points balance. 10,000 points = 10,000 VND */
  @Builder.Default
  @Column(nullable = false)
  private Long pointsBalance = 0L;

  /** Total points earned all time. */
  @Builder.Default
  @Column(nullable = false)
  private Long totalPointsEarned = 0L;

  /** Total points redeemed/used. */
  @Builder.Default
  @Column(nullable = false)
  private Long totalPointsRedeemed = 0L;

  /** Total amount spent in VND (for tracking). */
  @Builder.Default
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmountSpent = BigDecimal.ZERO;

  /** Add points to account. */
  public void addPoints(Long points) {
    this.pointsBalance += points;
    this.totalPointsEarned += points;
  }

  /**
   * Redeem points from account.
   *
   * @return true if successful, false if insufficient balance
   */
  public boolean redeemPoints(Long points) {
    if (this.pointsBalance < points) {
      return false;
    }
    this.pointsBalance -= points;
    this.totalPointsRedeemed += points;
    return true;
  }

  /** Check if user has enough points. */
  public boolean hasEnoughPoints(Long points) {
    return this.pointsBalance >= points;
  }
}
