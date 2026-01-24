package com.huynqb.laundrylockerbackend.module.loyalty.repository;

import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampTransactionType;
import com.huynqb.laundrylockerbackend.module.loyalty.model.StampTransaction;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StampTransactionRepository extends JpaRepository<StampTransaction, Long> {

  Page<StampTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  List<StampTransaction> findByStampCardIdOrderByCreatedAtDesc(Long stampCardId);

  List<StampTransaction> findByUserIdAndTypeOrderByCreatedAtDesc(
      Long userId, StampTransactionType type);

  List<StampTransaction> findByOrderId(Long orderId);
}
