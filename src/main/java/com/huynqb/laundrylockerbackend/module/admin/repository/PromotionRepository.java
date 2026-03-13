package com.huynqb.laundrylockerbackend.module.admin.repository;

import com.huynqb.laundrylockerbackend.module.admin.model.Promotion;
import com.huynqb.laundrylockerbackend.module.admin.model.Promotion.AcquisitionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Promotion entity. */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

  /** Find promotion by code. */
  Optional<Promotion> findByCode(String code);

  /** Check if code exists. */
  boolean existsByCode(String code);

  /** Find active promotions. */
  @Query(
      "SELECT p FROM Promotion p WHERE p.isActive = true "
          + "AND p.startDate <= :now AND p.endDate >= :now "
          + "AND (p.totalUsageLimit IS NULL OR p.currentUsageCount < p.totalUsageLimit) "
          + "AND p.acquisitionType <> 'POINTS' "
          + "ORDER BY p.priority DESC")
  List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

  /** Find promotions by status (custom query based on dates). */
  @Query(
      "SELECT p FROM Promotion p WHERE "
          + "(:status = 'ACTIVE' AND p.isActive = true AND p.startDate <= :now AND p.endDate >= :now) OR "
          + "(:status = 'UPCOMING' AND p.isActive = true AND p.startDate > :now) OR "
          + "(:status = 'EXPIRED' AND p.endDate < :now) OR "
          + "(:status = 'INACTIVE' AND p.isActive = false)")
  Page<Promotion> findByStatus(
      @Param("status") String status, @Param("now") LocalDateTime now, Pageable pageable);

  /** Find all promotions with pagination. */
  Page<Promotion> findAllByOrderByCreatedAtDesc(Pageable pageable);

  /** Find promotions expiring soon. */
  @Query(
      "SELECT p FROM Promotion p WHERE p.isActive = true "
          + "AND p.endDate BETWEEN :now AND :endDate "
          + "ORDER BY p.endDate ASC")
  List<Promotion> findExpiringSoon(
      @Param("now") LocalDateTime now, @Param("endDate") LocalDateTime endDate);

  /** Increment usage count. */
  @Modifying
  @Query("UPDATE Promotion p SET p.currentUsageCount = p.currentUsageCount + 1 WHERE p.id = :id")
  void incrementUsageCount(@Param("id") Long id);

  /** Decrement usage count (for cancelled orders). */
  @Modifying
  @Query(
      "UPDATE Promotion p SET p.currentUsageCount = CASE WHEN p.currentUsageCount > 0 THEN p.currentUsageCount - 1 ELSE 0 END WHERE p.id = :id")
  void decrementUsageCount(@Param("id") Long id);

  /** Deactivate expired promotions. */
  @Modifying
  @Query("UPDATE Promotion p SET p.isActive = false WHERE p.endDate < :now AND p.isActive = true")
  int deactivateExpiredPromotions(@Param("now") LocalDateTime now);

  /** Search promotions by code or title. */
  @Query(
      "SELECT p FROM Promotion p WHERE "
          + "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
          + "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Promotion> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

  // ========== LOYALTY REWARDS QUERIES (consolidated from loyalty_rewards)
  // ==========

  /** Find loyalty rewards (promotions with acquisition_type = POINTS). */
  @Query(
      "SELECT p FROM Promotion p WHERE p.acquisitionType = :type "
          + "AND p.isActive = true "
          + "AND (p.startDate IS NULL OR p.startDate <= :now) "
          + "AND (p.endDate IS NULL OR p.endDate >= :now) "
          + "ORDER BY p.pointsRequired ASC")
  List<Promotion> findByAcquisitionTypeAndActive(
      @Param("type") AcquisitionType type, @Param("now") LocalDateTime now);

  /**
   * Find POINTS-type promotions regardless of isActive flag. Used so the reward catalog is visible
   * even when isActive=false (e.g. sample data).
   */
  @Query(
      "SELECT p FROM Promotion p WHERE p.acquisitionType = :type "
          + "AND (p.startDate IS NULL OR p.startDate <= :now) "
          + "AND (p.endDate IS NULL OR p.endDate >= :now) "
          + "ORDER BY p.pointsRequired ASC NULLS LAST")
  List<Promotion> findByAcquisitionTypeWithinDateRange(
      @Param("type") AcquisitionType type, @Param("now") LocalDateTime now);

  /** Find loyalty rewards by category. */
  @Query(
      "SELECT p FROM Promotion p WHERE p.acquisitionType = 'POINTS' "
          + "AND p.category = :category AND p.isActive = true "
          + "ORDER BY p.pointsRequired ASC")
  List<Promotion> findLoyaltyRewardsByCategory(@Param("category") String category);

  /** Find loyalty rewards by minimum tier. */
  @Query(
      "SELECT p FROM Promotion p WHERE p.acquisitionType = 'POINTS' "
          + "AND (p.minimumTier IS NULL OR p.minimumTier = :tier) "
          + "AND p.isActive = true "
          + "ORDER BY p.pointsRequired ASC")
  List<Promotion> findLoyaltyRewardsAvailableForTier(@Param("tier") String tier);

  /** Find loyalty rewards with available quantity. */
  @Query(
      "SELECT p FROM Promotion p WHERE p.acquisitionType = 'POINTS' "
          + "AND p.isActive = true "
          + "AND (p.remainingQuantity IS NULL OR p.remainingQuantity > 0) "
          + "ORDER BY p.pointsRequired ASC")
  List<Promotion> findAvailableLoyaltyRewards();

  /** Decrement remaining quantity for a loyalty reward. */
  @Modifying
  @Query(
      "UPDATE Promotion p SET p.remainingQuantity = p.remainingQuantity - 1 "
          + "WHERE p.id = :id AND p.remainingQuantity > 0")
  int decrementRemainingQuantity(@Param("id") Long id);
}
