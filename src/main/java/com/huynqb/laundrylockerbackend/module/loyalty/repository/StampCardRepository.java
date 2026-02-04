package com.huynqb.laundrylockerbackend.module.loyalty.repository;

import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampType;
import com.huynqb.laundrylockerbackend.module.loyalty.model.StampCard;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StampCardRepository extends JpaRepository<StampCard, Long> {

  List<StampCard> findByUserId(Long userId);

  List<StampCard> findByUserIdAndStampType(Long userId, StampType stampType);

  Optional<StampCard> findByUserIdAndStampTypeAndBoxSize(
      Long userId, StampType stampType, String boxSize);

  Optional<StampCard> findByUserIdAndStampTypeAndServiceId(
      Long userId, StampType stampType, Long serviceId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT sc FROM StampCard sc WHERE sc.user.id = :userId AND sc.stampType = :stampType AND sc.boxSize = :boxSize")
  Optional<StampCard> findByUserIdAndStampTypeAndBoxSizeForUpdate(
      @Param("userId") Long userId,
      @Param("stampType") StampType stampType,
      @Param("boxSize") String boxSize);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT sc FROM StampCard sc WHERE sc.user.id = :userId AND sc.stampType = :stampType AND sc.service.id = :serviceId")
  Optional<StampCard> findByUserIdAndStampTypeAndServiceIdForUpdate(
      @Param("userId") Long userId,
      @Param("stampType") StampType stampType,
      @Param("serviceId") Long serviceId);

  @Query("SELECT sc FROM StampCard sc WHERE sc.user.id = :userId AND sc.freeRewardsAvailable > 0")
  List<StampCard> findCardsWithAvailableRewards(@Param("userId") Long userId);

  @Query("SELECT SUM(sc.totalRewardsRedeemed) FROM StampCard sc")
  Integer sumAllRewardsRedeemed();
}
