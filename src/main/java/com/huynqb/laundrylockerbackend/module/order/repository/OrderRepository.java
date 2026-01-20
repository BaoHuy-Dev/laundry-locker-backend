package com.huynqb.laundrylockerbackend.module.order.repository;

import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
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

  Page<Order> findByDeleteFlagFalse(Pageable pageable);

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
}
