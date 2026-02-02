package com.huynqb.laundrylockerbackend.module.order.repository;

import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Order entity. */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findBySenderId(Long senderId);

  List<Order> findByReceiverId(Long receiverId);

  List<Order> findByLockerId(Long lockerId);

  List<Order> findByStatus(OrderStatus status);

  Optional<Order> findByPinCode(String pinCode);

  Optional<Order> findByOrderCode(String orderCode);

  Page<Order> findByDeleteFlagFalse(Pageable pageable);

  // Customer: Get my orders with pagination
  Page<Order> findBySenderIdAndDeleteFlagFalse(Long senderId, Pageable pageable);

  // Customer: Get my orders by status
  Page<Order> findBySenderIdAndStatusAndDeleteFlagFalse(
      Long senderId, OrderStatus status, Pageable pageable);

  // Staff: Get orders by status list
  Page<Order> findByStatusInAndDeleteFlagFalse(List<OrderStatus> statuses, Pageable pageable);

  // Staff: Get orders assigned to staff
  Page<Order> findByStaffIdAndDeleteFlagFalse(Long staffId, Pageable pageable);

  @Query("SELECT o FROM Order o WHERE o.locker.store.id = :storeId AND o.deleteFlag = false")
  Page<Order> findByStoreId(@Param("storeId") Long storeId, Pageable pageable);

  @Query(
      "SELECT o FROM Order o WHERE o.locker.store.id = :storeId AND o.status = :status AND"
          + " o.deleteFlag = false")
  List<Order> findByStoreIdAndStatus(
      @Param("storeId") Long storeId, @Param("status") OrderStatus status);

  List<Order> findByStaffId(Long staffId);

  @Query(
      "SELECT o FROM Order o WHERE o.status IN :statuses AND o.locker.id = :lockerId AND"
          + " o.deleteFlag = false")
  List<Order> findByLockerIdAndStatusIn(
      @Param("lockerId") Long lockerId, @Param("statuses") List<OrderStatus> statuses);

  // ===== Scheduler Queries =====

  /**
   * Find orders in INITIALIZED status that were created before the specified time. Used for
   * auto-cancellation of unconfirmed orders.
   */
  @Query(
      "SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :beforeTime AND o.deleteFlag = false")
  List<Order> findByStatusAndCreatedAtBefore(
      @Param("status") OrderStatus status, @Param("beforeTime") LocalDateTime beforeTime);

  /**
   * Find orders in COMPLETED status that still have boxes assigned. Used for auto-releasing boxes
   * after pickup.
   */
  @Query(
      "SELECT o FROM Order o WHERE o.status = 'COMPLETED' AND o.completedAt < :beforeTime "
          + "AND (o.sendBox IS NOT NULL OR o.receiveBox IS NOT NULL) AND o.deleteFlag = false")
  List<Order> findCompletedOrdersWithBoxesBefore(@Param("beforeTime") LocalDateTime beforeTime);

  /**
   * Find orders in RETURNED status where completedAt is null (waiting for pickup). Used to track
   * orders ready for customer pickup.
   */
  @Query(
      "SELECT o FROM Order o WHERE o.status = 'RETURNED' AND o.createdAt < :beforeTime AND o.deleteFlag = false")
  List<Order> findReturnedOrdersBefore(@Param("beforeTime") LocalDateTime beforeTime);

  // ===== Partner Statistics Queries =====

  @Query(
      "SELECT COUNT(o) FROM Order o WHERE o.locker.store.id IN :storeIds AND o.deleteFlag = false")
  long countByStoreIds(@Param("storeIds") List<Long> storeIds);

  @Query(
      "SELECT COUNT(o) FROM Order o WHERE o.locker.store.id IN :storeIds AND o.status = :status AND o.deleteFlag = false")
  long countByStoreIdsAndStatus(
      @Param("storeIds") List<Long> storeIds, @Param("status") OrderStatus status);

  @Query(
      "SELECT COUNT(o) FROM Order o WHERE o.locker.store.id IN :storeIds AND o.status IN ('INITIALIZED', 'WAITING', 'COLLECTED', 'PROCESSING', 'READY', 'RETURNED') AND o.deleteFlag = false")
  long countPendingByStoreIds(@Param("storeIds") List<Long> storeIds);

  @Query(
      "SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.locker.store.id IN :storeIds AND o.status = 'COMPLETED' AND o.deleteFlag = false")
  java.math.BigDecimal sumRevenueByStoreIds(@Param("storeIds") List<Long> storeIds);

  // ===== Partner Order Management Queries =====

  @Query(
      "SELECT o FROM Order o WHERE o.locker.store.id IN :storeIds AND o.status = :status AND o.deleteFlag = false")
  Page<Order> findByStoreIdsAndStatus(
      @Param("storeIds") List<Long> storeIds,
      @Param("status") OrderStatus status,
      Pageable pageable);

  @Query("SELECT o FROM Order o WHERE o.locker.store.id IN :storeIds AND o.deleteFlag = false")
  Page<Order> findByStoreIds(@Param("storeIds") List<Long> storeIds, Pageable pageable);

  // ===== Partner Date Range Statistics Queries =====

  @Query(
      "SELECT COUNT(o) FROM Order o WHERE o.locker.store.id IN :storeIds "
          + "AND o.createdAt >= :fromDate AND o.createdAt < :toDate AND o.deleteFlag = false")
  long countByStoreIdsAndDateRange(
      @Param("storeIds") List<Long> storeIds,
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate);

  @Query(
      "SELECT COUNT(o) FROM Order o WHERE o.locker.store.id IN :storeIds "
          + "AND o.status = :status AND o.createdAt >= :fromDate AND o.createdAt < :toDate AND o.deleteFlag = false")
  long countByStoreIdsAndStatusAndDateRange(
      @Param("storeIds") List<Long> storeIds,
      @Param("status") OrderStatus status,
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate);

  @Query(
      "SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.locker.store.id IN :storeIds "
          + "AND o.status = 'COMPLETED' AND o.createdAt >= :fromDate AND o.createdAt < :toDate AND o.deleteFlag = false")
  java.math.BigDecimal sumRevenueByStoreIdsAndDateRange(
      @Param("storeIds") List<Long> storeIds,
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate);
}
