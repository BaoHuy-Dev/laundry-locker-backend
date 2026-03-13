package com.huynqb.laundrylockerbackend.module.partner.helper;

import com.huynqb.laundrylockerbackend.core.util.CodeGenerator;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Helper class for order box operations. Follows Single Responsibility Principle - handles
 * box-related logic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderBoxHelper {

  private final BoxRepository boxRepository;

  /**
   * Get boxes for the specified action (COLLECT or RETURN).
   *
   * @param order the order
   * @param action the access code action
   * @return list of boxes
   */
  public List<Box> getBoxesForAction(Order order, AccessCodeAction action) {
    List<Box> boxes = new ArrayList<>();

    if (action == AccessCodeAction.COLLECT) {
      addSendBoxes(order, boxes);
    } else if (action == AccessCodeAction.RETURN) {
      addReceiveBoxes(order, boxes);
    }

    return boxes;
  }

  /**
   * Update order status and boxes after unlock.
   *
   * @param order the order
   * @param action the access code action
   */
  public void updateOrderAndBoxesAfterUnlock(Order order, AccessCodeAction action) {
    if (action == AccessCodeAction.COLLECT) {
      handleCollectAction(order);
    } else if (action == AccessCodeAction.RETURN) {
      handleReturnAction(order);
    }
  }

  /**
   * Release all send boxes to AVAILABLE status.
   *
   * @param order the order
   */
  public void releaseSendBoxes(Order order) {
    if (order.getSendBox() != null) {
      updateBoxStatus(order.getSendBox(), BoxStatus.AVAILABLE);
    }
    if (order.getSendBoxes() != null) {
      order.getSendBoxes().forEach(box -> updateBoxStatus(box, BoxStatus.AVAILABLE));
    }
  }

  /**
   * Mark receive boxes as OCCUPIED.
   *
   * @param boxes the boxes to mark
   */
  public void markBoxesAsOccupied(List<Box> boxes) {
    boxes.forEach(box -> updateBoxStatus(box, BoxStatus.OCCUPIED));
  }

  // ===== Private Methods =====

  private void addSendBoxes(Order order, List<Box> boxes) {
    if (order.getSendBox() != null) {
      boxes.add(order.getSendBox());
    }
    if (order.getSendBoxes() != null) {
      boxes.addAll(order.getSendBoxes());
    }
  }

  private void addReceiveBoxes(Order order, List<Box> boxes) {
    if (order.getReceiveBox() != null) {
      boxes.add(order.getReceiveBox());
    }
    if (order.getReceiveBoxes() != null) {
      boxes.addAll(order.getReceiveBoxes());
    }

    // Fallback for legacy/partner flow: READY orders may not have receive boxes
    // assigned yet.
    // In that case, reuse send boxes so staff can still open the locker to return
    // items.
    if (boxes.isEmpty()) {
      addSendBoxes(order, boxes);
      if (!boxes.isEmpty()) {
        log.warn(
            "Order {} has no receive boxes for RETURN action, fallback to send boxes",
            order.getId());
      }
    }
  }

  private void handleCollectAction(Order order) {
    order.setStatus(OrderStatus.COLLECTED);
    releaseSendBoxes(order);
    log.debug("Order {} status updated to COLLECTED, send boxes released", order.getId());
  }

  private void handleReturnAction(Order order) {
    // Ensure receive box fields are set for customer pickup PIN flow.
    if (order.getReceiveBox() == null && order.getSendBox() != null) {
      order.setReceiveBox(order.getSendBox());
    }
    if ((order.getReceiveBoxes() == null || order.getReceiveBoxes().isEmpty())
        && order.getSendBoxes() != null
        && !order.getSendBoxes().isEmpty()) {
      order.setReceiveBoxes(new HashSet<>(order.getSendBoxes()));
    }

    order.setStatus(OrderStatus.RETURNED);
    order.setPinCode(CodeGenerator.generatePinCode());
    order.setPinCodeIssuedAt(LocalDateTime.now());
    log.debug("Order {} status updated to RETURNED, new PIN generated", order.getId());
  }

  private void updateBoxStatus(Box box, BoxStatus status) {
    box.setStatus(status);
    boxRepository.save(box);
  }
}
