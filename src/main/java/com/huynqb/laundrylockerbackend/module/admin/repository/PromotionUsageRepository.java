package com.huynqb.laundrylockerbackend.module.admin.repository;

import com.huynqb.laundrylockerbackend.module.admin.entity.PromotionUsage;
import com.huynqb.laundrylockerbackend.module.admin.entity.PromotionUsage.UsageStatus;
import com.huynqb.laundrylockerbackend.module.admin.entity.PromotionUsage.UsageType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for PromotionUsage entity. */
@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

  /** Find usage by user ID. */
  Page<PromotionUsage> findByUserIdOrderByUsedAtDesc(Long userId, Pageable pageable);

  /** Find usage by promotion ID. */
  Page<PromotionUsage> findByPromotionIdOrderByUsedAtDesc(Long promotionId, Pageable pageable);

  /** Find usage by user and promotion. */
  Optional<PromotionUsage> findByPromotionIdAndUserIdAndOrderId(
      Long promotionId, Long userId, Long orderId);

  /** Count usage by user and promotion. */
  long countByPromotionIdAndUserId(Long promotionId, Long userId);

  /** Find user's active redemptions (loyalty rewards). */
  @Query(
      "SELECT pu FROM PromotionUsage pu WHERE pu.user.id = :userId "
          + "AND pu.usageType = :usageType AND pu.status = :status ORDER BY pu.usedAt DESC")
  List<PromotionUsage> findByUserIdAndUsageTypeAndStatus(
      @Param("userId") Long userId,
      @Param("usageType") UsageType usageType,
      @Param("status") UsageStatus status);

  /** Find by reward code. */
  Optional<PromotionUsage> findByRewardCode(String rewardCode);

  /** Find expired but still active usages. */
  @Query(
      "SELECT pu FROM PromotionUsage pu WHERE pu.status = 'ACTIVE' "
          + "AND pu.expiresAt IS NOT NULL AND pu.expiresAt < CURRENT_TIMESTAMP")
  List<PromotionUsage> findExpiredActiveUsages();

  /** Check if user has used a promotion. */
  boolean existsByPromotionIdAndUserIdAndStatus(Long promotionId, Long userId, UsageStatus status);

  /** Get usage statistics for a promotion. */
  @Query(
      "SELECT COUNT(pu), SUM(pu.discountApplied), SUM(pu.pointsSpent) "
          + "FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId")
  Object[] getPromotionUsageStats(@Param("promotionId") Long promotionId);

  /** Find redemptions by order. */
  List<PromotionUsage> findByOrderIdOrUsedOrderId(Long orderId, Long usedOrderId);
}
