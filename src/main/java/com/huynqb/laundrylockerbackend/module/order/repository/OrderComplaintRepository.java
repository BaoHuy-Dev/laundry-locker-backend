package com.huynqb.laundrylockerbackend.module.order.repository;

import com.huynqb.laundrylockerbackend.module.order.model.OrderComplaint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for OrderComplaint entity. */
@Repository
public interface OrderComplaintRepository extends JpaRepository<OrderComplaint, Long> {

  /** Find all complaints for a specific order. */
  List<OrderComplaint> findByOrderIdOrderByCreatedAtDesc(Long orderId);

  /** Find all complaints by a specific user, most recent first. */
  List<OrderComplaint> findByUserIdOrderByCreatedAtDesc(Long userId);

  /** Check if a complaint already exists for an order by a specific user. */
  boolean existsByOrderIdAndUserId(Long orderId, Long userId);
}
