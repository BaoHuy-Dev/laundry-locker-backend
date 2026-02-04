package com.huynqb.laundrylockerbackend.module.payment.repository;

import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentStatus;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Payment entity. */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  List<Payment> findByOrderId(Long orderId);

  List<Payment> findByCustomerId(Long customerId);

  Optional<Payment> findByReferenceId(String referenceId);

  List<Payment> findByStatus(PaymentStatus status);

  Optional<Payment> findFirstByOrderIdAndStatus(Long orderId, PaymentStatus status);

  // Pagination methods for Admin
  Page<Payment> findAll(Pageable pageable);

  Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
