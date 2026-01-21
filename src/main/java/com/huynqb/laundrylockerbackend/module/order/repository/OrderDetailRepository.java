package com.huynqb.laundrylockerbackend.module.order.repository;

import com.huynqb.laundrylockerbackend.module.order.model.OrderDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for OrderDetail entity. */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

  List<OrderDetail> findByOrderId(Long orderId);

  void deleteByOrderId(Long orderId);
}
