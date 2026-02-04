package com.huynqb.laundrylockerbackend.module.loyalty.enums;

/** Types of point transactions. */
public enum PointTransactionType {
  /** Points earned from order payment. */
  EARN,

  /** Points redeemed for discount. */
  REDEEM,

  /** Points expired (if expiration is implemented). */
  EXPIRE,

  /** Points adjusted by admin. */
  ADJUST,

  /** Bonus points from promotions. */
  BONUS,

  /** Points refunded from cancelled order. */
  REFUND
}
