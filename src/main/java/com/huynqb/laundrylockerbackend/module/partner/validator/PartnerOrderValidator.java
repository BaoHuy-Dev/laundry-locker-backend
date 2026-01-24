package com.huynqb.laundrylockerbackend.module.partner.validator;

import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import com.huynqb.laundrylockerbackend.module.partner.model.Partner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Validator for partner order operations. Follows Single Responsibility Principle - only handles
 * validation logic.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PartnerOrderValidator {

  /**
   * Validate that order belongs to partner's store.
   *
   * @param order the order to validate
   * @param partnerId the partner ID
   * @throws RuntimeException if order doesn't belong to partner
   */
  public static void validateOrderBelongsToPartner(Order order, Long partnerId) {
    if (order.getLocker() == null
        || order.getLocker().getStore() == null
        || order.getLocker().getStore().getPartner() == null
        || !order.getLocker().getStore().getPartner().getId().equals(partnerId)) {
      throw new RuntimeException("Order does not belong to partner's store");
    }
  }

  /**
   * Validate that order belongs to partner's store.
   *
   * @param order the order to validate
   * @param partner the partner
   * @throws RuntimeException if order doesn't belong to partner
   */
  public static void validateOrderBelongsToPartner(Order order, Partner partner) {
    validateOrderBelongsToPartner(order, partner.getId());
  }

  /**
   * Validate order status for access code action.
   *
   * @param order the order to validate
   * @param action the access code action
   * @throws RuntimeException if status is invalid for the action
   */
  public static void validateOrderStatusForAction(Order order, AccessCodeAction action) {
    OrderStatus requiredStatus = getRequiredStatusForAction(action);
    if (order.getStatus() != requiredStatus) {
      throw new RuntimeException(
          String.format(
              "Order must be in %s status to generate %s code. Current: %s",
              requiredStatus, action, order.getStatus()));
    }
  }

  /**
   * Check if order status is valid for unlock action.
   *
   * @param order the order to check
   * @param action the access code action
   * @return true if status is valid
   */
  public static boolean isValidStatusForUnlock(Order order, AccessCodeAction action) {
    OrderStatus requiredStatus = getRequiredStatusForAction(action);
    return order.getStatus() == requiredStatus;
  }

  /**
   * Get required order status for access code action.
   *
   * @param action the access code action
   * @return required order status
   */
  public static OrderStatus getRequiredStatusForAction(AccessCodeAction action) {
    return switch (action) {
      case COLLECT -> OrderStatus.WAITING;
      case RETURN -> OrderStatus.READY;
    };
  }

  /**
   * Validate order status for processing transition.
   *
   * @param order the order to validate
   * @throws RuntimeException if status is invalid
   */
  public static void validateForProcessing(Order order) {
    if (order.getStatus() != OrderStatus.COLLECTED) {
      throw new RuntimeException(
          "Order must be in COLLECTED status. Current: " + order.getStatus());
    }
  }

  /**
   * Validate order status for ready transition.
   *
   * @param order the order to validate
   * @throws RuntimeException if status is invalid
   */
  public static void validateForReady(Order order) {
    if (order.getStatus() != OrderStatus.PROCESSING && order.getStatus() != OrderStatus.COLLECTED) {
      throw new RuntimeException(
          "Order must be in COLLECTED or PROCESSING status. Current: " + order.getStatus());
    }
  }

  /**
   * Validate order status for acceptance.
   *
   * @param order the order to validate
   * @throws RuntimeException if status is invalid
   */
  public static void validateForAcceptance(Order order) {
    if (order.getStatus() != OrderStatus.WAITING) {
      throw new RuntimeException("Order must be in WAITING status. Current: " + order.getStatus());
    }
  }
}
