package com.huynqb.laundrylockerbackend.module.partner.repository;

import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeStatus;
import com.huynqb.laundrylockerbackend.module.partner.model.Partner;
import com.huynqb.laundrylockerbackend.module.partner.model.StaffAccessCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffAccessCodeRepository extends JpaRepository<StaffAccessCode, Long> {

  /** Find by code. */
  Optional<StaffAccessCode> findByCode(String code);

  /** Find by code and status. */
  Optional<StaffAccessCode> findByCodeAndStatus(String code, AccessCodeStatus status);

  /** Find active code by code string. */
  @Query(
      "SELECT s FROM StaffAccessCode s WHERE s.code = :code AND s.status = 'ACTIVE' "
          + "AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
  Optional<StaffAccessCode> findValidCode(
      @Param("code") String code, @Param("now") LocalDateTime now);

  /** Find codes by order. */
  List<StaffAccessCode> findByOrderOrderByCreatedAtDesc(Order order);

  /** Find codes by order ID. */
  List<StaffAccessCode> findByOrderIdOrderByCreatedAtDesc(Long orderId);

  /** Find codes by partner. */
  Page<StaffAccessCode> findByPartnerOrderByCreatedAtDesc(Partner partner, Pageable pageable);

  /** Find codes by partner ID. */
  Page<StaffAccessCode> findByPartnerIdOrderByCreatedAtDesc(Long partnerId, Pageable pageable);

  /** Find active code for order and action. */
  @Query(
      "SELECT s FROM StaffAccessCode s WHERE s.order.id = :orderId AND s.action = :action "
          + "AND s.status = 'ACTIVE' AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
  Optional<StaffAccessCode> findActiveCodeForOrderAndAction(
      @Param("orderId") Long orderId,
      @Param("action") AccessCodeAction action,
      @Param("now") LocalDateTime now);

  /** Check if active code exists for order. */
  boolean existsByOrderIdAndStatus(Long orderId, AccessCodeStatus status);

  /** Find expired active codes (for cleanup). */
  @Query("SELECT s FROM StaffAccessCode s WHERE s.status = 'ACTIVE' AND s.expiresAt < :now")
  List<StaffAccessCode> findExpiredCodes(@Param("now") LocalDateTime now);

  /** Count codes by partner and status. */
  long countByPartnerIdAndStatus(Long partnerId, AccessCodeStatus status);
}
