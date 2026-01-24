package com.huynqb.laundrylockerbackend.module.loyalty.repository;

import com.huynqb.laundrylockerbackend.module.loyalty.enums.PointTransactionType;
import com.huynqb.laundrylockerbackend.module.loyalty.model.PointTransaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

  Page<PointTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  List<PointTransaction> findByUserIdAndTypeOrderByCreatedAtDesc(
      Long userId, PointTransactionType type);

  @Query(
      "SELECT pt FROM PointTransaction pt WHERE pt.user.id = :userId AND pt.createdAt BETWEEN :startDate AND :endDate ORDER BY pt.createdAt DESC")
  List<PointTransaction> findByUserIdAndDateRange(
      @Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.user.id = :userId AND pt.type = :type")
  Long sumPointsByUserIdAndType(
      @Param("userId") Long userId, @Param("type") PointTransactionType type);

  List<PointTransaction> findByOrderId(Long orderId);
}
