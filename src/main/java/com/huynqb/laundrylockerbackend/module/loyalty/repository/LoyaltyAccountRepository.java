package com.huynqb.laundrylockerbackend.module.loyalty.repository;

import com.huynqb.laundrylockerbackend.module.loyalty.model.LoyaltyAccount;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {

  Optional<LoyaltyAccount> findByUserId(Long userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT la FROM LoyaltyAccount la WHERE la.user.id = :userId")
  Optional<LoyaltyAccount> findByUserIdForUpdate(@Param("userId") Long userId);

  boolean existsByUserId(Long userId);

  @Query("SELECT SUM(la.pointsBalance) FROM LoyaltyAccount la")
  Long sumAllPointsBalance();

  @Query("SELECT SUM(la.totalPointsEarned) FROM LoyaltyAccount la")
  Long sumAllPointsEarned();

  @Query("SELECT SUM(la.totalPointsRedeemed) FROM LoyaltyAccount la")
  Long sumAllPointsRedeemed();
}
