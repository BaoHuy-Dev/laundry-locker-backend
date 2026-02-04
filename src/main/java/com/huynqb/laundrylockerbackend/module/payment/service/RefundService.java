package com.huynqb.laundrylockerbackend.module.payment.service;

import com.huynqb.laundrylockerbackend.core.exception.BusinessException;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.payment.dto.request.RefundRequest;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.RefundResponse;
import com.huynqb.laundrylockerbackend.module.payment.entity.Refund;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import com.huynqb.laundrylockerbackend.module.payment.repository.PaymentRepository;
import com.huynqb.laundrylockerbackend.module.payment.repository.RefundRepository;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing refunds. */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

  private final RefundRepository refundRepository;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;

  /** Request a refund for a payment. */
  @Transactional
  public RefundResponse requestRefund(Long paymentId, RefundRequest request, Long requesterId) {
    log.info("Refund requested for payment: {} by user: {}", paymentId, requesterId);

    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        "E_REFUND001", HttpStatus.NOT_FOUND, "Payment not found"));

    Order order = payment.getOrder();

    // Validate refund amount
    BigDecimal alreadyRefunded = refundRepository.sumRefundedAmountByPaymentId(paymentId);
    BigDecimal availableForRefund = payment.getAmount().subtract(alreadyRefunded);

    if (request.getAmount().compareTo(availableForRefund) > 0) {
      throw new BusinessException(
          "E_REFUND002",
          HttpStatus.BAD_REQUEST,
          "Refund amount exceeds available amount. Available: " + availableForRefund);
    }

    // Check for pending refunds
    if (refundRepository.existsByPaymentIdAndStatus(paymentId, Refund.RefundStatus.PENDING)) {
      throw new BusinessException(
          "E_REFUND003",
          HttpStatus.CONFLICT,
          "A refund request is already pending for this payment");
    }

    // Create refund request
    Refund refund =
        Refund.builder()
            .payment(payment)
            .order(order)
            .amount(request.getAmount())
            .reason(request.getReason())
            .notes(request.getNotes())
            .status(Refund.RefundStatus.PENDING)
            .requestedAt(LocalDateTime.now())
            .build();

    refund = refundRepository.save(refund);
    log.info("Refund request created with ID: {}", refund.getId());

    return mapToResponse(refund, payment.getAmount());
  }

  /** Process a refund (admin only). */
  @Transactional
  public RefundResponse processRefund(Long refundId, boolean approve, Long adminId, String notes) {
    log.info("Processing refund: {}, approve: {}, by admin: {}", refundId, approve, adminId);

    Refund refund =
        refundRepository
            .findById(refundId)
            .orElseThrow(
                () ->
                    new BusinessException("E_REFUND004", HttpStatus.NOT_FOUND, "Refund not found"));

    if (refund.getStatus() != Refund.RefundStatus.PENDING) {
      throw new BusinessException(
          "E_REFUND005", HttpStatus.BAD_REQUEST, "Refund has already been processed");
    }

    User admin =
        userRepository
            .findById(adminId)
            .orElseThrow(
                () ->
                    new BusinessException("E_REFUND006", HttpStatus.NOT_FOUND, "Admin not found"));

    if (approve) {
      // Process the actual refund with payment gateway
      // This would call the payment gateway's refund API
      boolean gatewaySuccess = processRefundWithGateway(refund);

      if (gatewaySuccess) {
        refund.setStatus(Refund.RefundStatus.COMPLETED);
        refund.setTransactionId("REF-" + System.currentTimeMillis());
      } else {
        refund.setStatus(Refund.RefundStatus.FAILED);
        refund.setGatewayResponse("Payment gateway refund failed");
      }
    } else {
      refund.setStatus(Refund.RefundStatus.CANCELLED);
    }

    refund.setProcessedAt(LocalDateTime.now());
    refund.setProcessedBy(admin);
    if (notes != null) {
      refund.setNotes(refund.getNotes() + "\n[Admin]: " + notes);
    }

    refund = refundRepository.save(refund);
    log.info("Refund {} processed with status: {}", refundId, refund.getStatus());

    return mapToResponse(refund, refund.getPayment().getAmount());
  }

  /** Get refund by ID. */
  public RefundResponse getRefund(Long refundId) {
    Refund refund =
        refundRepository
            .findById(refundId)
            .orElseThrow(
                () ->
                    new BusinessException("E_REFUND004", HttpStatus.NOT_FOUND, "Refund not found"));
    return mapToResponse(refund, refund.getPayment().getAmount());
  }

  /** Get refunds for a payment. */
  public List<RefundResponse> getRefundsByPayment(Long paymentId) {
    return refundRepository.findByPaymentId(paymentId).stream()
        .map(r -> mapToResponse(r, r.getPayment().getAmount()))
        .toList();
  }

  /** Get refunds for an order. */
  public List<RefundResponse> getRefundsByOrder(Long orderId) {
    return refundRepository.findByOrderId(orderId).stream()
        .map(r -> mapToResponse(r, r.getPayment().getAmount()))
        .toList();
  }

  /** Get pending refunds (admin). */
  public List<RefundResponse> getPendingRefunds() {
    return refundRepository.findPendingRefunds().stream()
        .map(r -> mapToResponse(r, r.getPayment().getAmount()))
        .toList();
  }

  /** Get refunds by user. */
  public Page<RefundResponse> getRefundsByUser(Long userId, Pageable pageable) {
    return refundRepository
        .findByUserId(userId, pageable)
        .map(r -> mapToResponse(r, r.getPayment().getAmount()));
  }

  /** Get refunds by status. */
  public Page<RefundResponse> getRefundsByStatus(Refund.RefundStatus status, Pageable pageable) {
    return refundRepository
        .findByStatus(status, pageable)
        .map(r -> mapToResponse(r, r.getPayment().getAmount()));
  }

  // ===== Private Helper Methods =====

  private boolean processRefundWithGateway(Refund refund) {
    // TODO: Implement actual payment gateway refund
    // For now, simulate success
    log.info("Processing refund with payment gateway for amount: {}", refund.getAmount());
    return true;
  }

  private RefundResponse mapToResponse(Refund refund, BigDecimal originalAmount) {
    return RefundResponse.builder()
        .id(refund.getId())
        .paymentId(refund.getPayment().getId())
        .orderId(refund.getOrder().getId())
        .amount(refund.getAmount())
        .originalAmount(originalAmount)
        .status(refund.getStatus().name())
        .reason(refund.getReason())
        .transactionId(refund.getTransactionId())
        .requestedAt(refund.getRequestedAt())
        .processedAt(refund.getProcessedAt())
        .notes(refund.getNotes())
        .processedBy(refund.getProcessedBy() != null ? refund.getProcessedBy().getId() : null)
        .build();
  }
}
