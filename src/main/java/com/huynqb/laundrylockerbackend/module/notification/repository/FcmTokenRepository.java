package com.huynqb.laundrylockerbackend.module.notification.repository;

import com.huynqb.laundrylockerbackend.module.notification.model.FcmToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** Repository for FcmToken entity. */
@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

  /** Find all FCM tokens for a user. */
  List<FcmToken> findByUserId(Long userId);

  /** Find a specific token. */
  Optional<FcmToken> findByToken(String token);

  /** Find by user ID and token. */
  Optional<FcmToken> findByUserIdAndToken(Long userId, String token);

  /** Delete a specific token for a user. */
  void deleteByUserIdAndToken(Long userId, String token);

  /** Delete all tokens for a user. */
  void deleteByUserId(Long userId);

  /** Get all distinct tokens (for broadcast). */
  @Query("SELECT DISTINCT f.token FROM FcmToken f")
  List<String> findAllDistinctTokens();
}
