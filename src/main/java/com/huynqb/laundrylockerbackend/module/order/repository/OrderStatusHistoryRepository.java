package com.huynqb.laundrylockerbackend.module.order.repository;

import com.huynqb.laundrylockerbackend.module.order.entity.OrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

  List<OrderStatusHistory> findByOrderIdOrderByChangedAtAsc(Long orderId);

  @Query("SELECT h FROM OrderStatusHistory h WHERE h.order.id = :orderId ORDER BY h.changedAt DESC")
  List<OrderStatusHistory> findByOrderIdOrderByChangedAtDesc(@Param("orderId") Long orderId);

  @Query("SELECT h FROM OrderStatusHistory h WHERE h.order.id = :orderId AND h.toStatus = :status")
  List<OrderStatusHistory> findByOrderIdAndStatus(
      @Param("orderId") Long orderId, @Param("status") String status);
}
