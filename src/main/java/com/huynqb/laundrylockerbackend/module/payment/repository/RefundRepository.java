package com.huynqb.laundrylockerbackend.module.payment.repository;

import com.huynqb.laundrylockerbackend.module.payment.model.Refund;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Refund entity. */
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

  /** Find refunds by payment ID. */
  List<Refund> findByPaymentId(Long paymentId);

  /** Find refunds by order ID. */
  List<Refund> findByOrderId(Long orderId);

  /** Find refunds by status. */
  Page<Refund> findByStatus(Refund.RefundStatus status, Pageable pageable);

  /** Find pending refunds. */
  @Query("SELECT r FROM Refund r WHERE r.status = 'PENDING' ORDER BY r.requestedAt ASC")
  List<Refund> findPendingRefunds();

  /** Find refunds by user (through order). */
  @Query("SELECT r FROM Refund r WHERE r.order.sender.id = :userId ORDER BY r.requestedAt DESC")
  Page<Refund> findByUserId(@Param("userId") Long userId, Pageable pageable);

  /** Find refunds by staff (through order). */
  @Query("SELECT r FROM Refund r WHERE r.order.staff.id = :staffId ORDER BY r.requestedAt DESC")
  Page<Refund> findByStaffId(@Param("staffId") Long staffId, Pageable pageable);

  /** Find refunds requested within a date range. */
  @Query(
      "SELECT r FROM Refund r WHERE r.requestedAt BETWEEN :startDate AND :endDate ORDER BY r.requestedAt DESC")
  List<Refund> findByRequestedAtBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  /** Sum total refunded amount by order. */
  @Query(
      "SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.order.id = :orderId AND r.status = 'COMPLETED'")
  BigDecimal sumRefundedAmountByOrderId(@Param("orderId") Long orderId);

  /** Sum total refunded amount by payment. */
  @Query(
      "SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.payment.id = :paymentId AND r.status = 'COMPLETED'")
  BigDecimal sumRefundedAmountByPaymentId(@Param("paymentId") Long paymentId);

  /** Check if refund exists for payment. */
  boolean existsByPaymentIdAndStatus(Long paymentId, Refund.RefundStatus status);

  /** Find latest refund for an order. */
  Optional<Refund> findFirstByOrderIdOrderByRequestedAtDesc(Long orderId);
}
