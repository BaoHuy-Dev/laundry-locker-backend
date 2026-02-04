package com.huynqb.laundrylockerbackend.module.admin.service;

import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentResponse;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentStatus;
import com.huynqb.laundrylockerbackend.module.payment.mapper.PaymentMapper;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import com.huynqb.laundrylockerbackend.module.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for admin payment management. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;

  /** Get all payments with optional status filter. */
  @Transactional(readOnly = true)
  public Page<PaymentResponse> getAllPayments(PaymentStatus status, Pageable pageable) {
    log.info("Admin getting all payments, status filter: {}", status);
    if (status != null) {
      return paymentRepository.findByStatus(status, pageable).map(paymentMapper::toResponse);
    }
    return paymentRepository.findAll(pageable).map(paymentMapper::toResponse);
  }

  /** Get payment by ID. */
  @Transactional(readOnly = true)
  public PaymentResponse getPaymentById(Long paymentId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    return paymentMapper.toResponse(payment);
  }

  /** Update payment status (admin override). */
  @Transactional
  public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
    log.info("Admin updating payment {} status to {}", paymentId, newStatus);

    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

    payment.setStatus(newStatus);
    payment = paymentRepository.save(payment);

    log.info("Payment {} status updated to {}", paymentId, newStatus);
    return paymentMapper.toResponse(payment);
  }
}
