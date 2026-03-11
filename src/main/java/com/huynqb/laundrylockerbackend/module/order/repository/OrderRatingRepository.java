package com.huynqb.laundrylockerbackend.module.order.repository;

import com.huynqb.laundrylockerbackend.module.order.model.OrderRating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRatingRepository extends JpaRepository<OrderRating, Long> {

  Optional<OrderRating> findByOrderIdAndUserId(Long orderId, Long userId);

  Optional<OrderRating> findByOrderId(Long orderId);

  boolean existsByOrderIdAndUserId(Long orderId, Long userId);

  @Query(
      "SELECT r FROM OrderRating r WHERE r.order.locker.store.id = :storeId AND r.isVisible = true AND r.deleteFlag = false")
  Page<OrderRating> findByStoreId(@Param("storeId") Long storeId, Pageable pageable);

  @Query(
      "SELECT r FROM OrderRating r WHERE r.order.staff.id = :staffId AND r.isVisible = true AND r.deleteFlag = false")
  Page<OrderRating> findByStaffId(@Param("staffId") Long staffId, Pageable pageable);

  @Query(
      "SELECT AVG(r.rating) FROM OrderRating r WHERE r.order.locker.store.id = :storeId AND r.isVisible = true AND r.deleteFlag = false")
  Double getAverageRatingByStoreId(@Param("storeId") Long storeId);

  @Query(
      "SELECT AVG(r.rating) FROM OrderRating r WHERE r.order.staff.id = :staffId AND r.isVisible = true AND r.deleteFlag = false")
  Double getAverageRatingByStaffId(@Param("staffId") Long staffId);

  @Query(
      "SELECT COUNT(r) FROM OrderRating r WHERE r.order.locker.store.id = :storeId AND r.isVisible = true AND r.deleteFlag = false")
  Long countByStoreId(@Param("storeId") Long storeId);

  @Query(
      "SELECT COUNT(r) FROM OrderRating r WHERE r.order.staff.id = :staffId AND r.isVisible = true AND r.deleteFlag = false")
  Long countByStaffId(@Param("staffId") Long staffId);

  @Query(
      "SELECT r.rating, COUNT(r) FROM OrderRating r WHERE r.order.locker.store.id = :storeId AND r.isVisible = true AND r.deleteFlag = false GROUP BY r.rating")
  List<Object[]> getRatingDistributionByStoreId(@Param("storeId") Long storeId);

  List<OrderRating> findByUserIdAndDeleteFlagFalse(Long userId);
}
