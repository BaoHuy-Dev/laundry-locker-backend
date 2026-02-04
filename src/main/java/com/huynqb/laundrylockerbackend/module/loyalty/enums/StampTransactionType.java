package com.huynqb.laundrylockerbackend.module.loyalty.enums;

/** Types of stamp transactions. */
public enum StampTransactionType {
  /** Stamp earned from usage. */
  EARN,

  /** Free reward redeemed. */
  REDEEM,

  /** Stamps adjusted by admin. */
  ADJUST,

  /** Bonus stamps from promotions. */
  BONUS
}
