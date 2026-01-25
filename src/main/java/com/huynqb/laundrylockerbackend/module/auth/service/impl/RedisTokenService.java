package com.huynqb.laundrylockerbackend.module.auth.service.impl;

import com.huynqb.laundrylockerbackend.module.auth.service.TokenService;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis implementation of TokenService Uses Redis for high-performance token storage with automatic
 * TTL expiration.
 *
 * <p>Key patterns: - blacklist:{token} -> "1" (TTL = token remaining lifetime) - refresh:{token} ->
 * userId (TTL = refresh token lifetime) - user:refresh:{userId} -> Set of refresh tokens (for
 * logout all devices)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService implements TokenService {

  private final StringRedisTemplate redisTemplate;

  private static final String BLACKLIST_PREFIX = "blacklist:";
  private static final String REFRESH_PREFIX = "refresh:";
  private static final String USER_REFRESH_PREFIX = "user:refresh:";

  @Override
  public void blacklistAccessToken(String token, long expirationMs) {
    String key = BLACKLIST_PREFIX + token;
    redisTemplate.opsForValue().set(key, "1", expirationMs, TimeUnit.MILLISECONDS);
    log.debug(
        "Redis - Blacklisted access token: {}..., TTL: {}ms",
        token.substring(0, Math.min(20, token.length())),
        expirationMs);
  }

  @Override
  public boolean isAccessTokenBlacklisted(String token) {
    String key = BLACKLIST_PREFIX + token;
    Boolean exists = redisTemplate.hasKey(key);
    return Boolean.TRUE.equals(exists);
  }

  @Override
  public void saveRefreshToken(String token, Long userId, long expirationMs) {
    String refreshKey = REFRESH_PREFIX + token;
    String userRefreshKey = USER_REFRESH_PREFIX + userId;

    // Save refresh token -> userId mapping
    redisTemplate
        .opsForValue()
        .set(refreshKey, userId.toString(), expirationMs, TimeUnit.MILLISECONDS);

    // Add to user's refresh token set (for logout all devices)
    redisTemplate.opsForSet().add(userRefreshKey, token);
    redisTemplate.expire(userRefreshKey, expirationMs, TimeUnit.MILLISECONDS);

    log.debug(
        "Redis - Saved refresh token for user {}: {}...",
        userId,
        token.substring(0, Math.min(20, token.length())));
  }

  @Override
  public Long getUserIdByRefreshToken(String token) {
    String key = REFRESH_PREFIX + token;
    String userId = redisTemplate.opsForValue().get(key);
    if (userId != null) {
      try {
        return Long.parseLong(userId);
      } catch (NumberFormatException e) {
        log.error("Redis - Invalid userId format for refresh token: {}", token);
        return null;
      }
    }
    return null;
  }

  @Override
  public void deleteRefreshToken(String token) {
    String refreshKey = REFRESH_PREFIX + token;

    // Get userId first to remove from user's set
    String userId = redisTemplate.opsForValue().get(refreshKey);
    if (userId != null) {
      String userRefreshKey = USER_REFRESH_PREFIX + userId;
      redisTemplate.opsForSet().remove(userRefreshKey, token);
    }

    // Delete the refresh token
    redisTemplate.delete(refreshKey);
    log.debug(
        "Redis - Deleted refresh token: {}...", token.substring(0, Math.min(20, token.length())));
  }

  @Override
  public void deleteAllUserRefreshTokens(Long userId) {
    String userRefreshKey = USER_REFRESH_PREFIX + userId;

    // Get all refresh tokens for this user
    Set<String> tokens = redisTemplate.opsForSet().members(userRefreshKey);

    if (tokens != null && !tokens.isEmpty()) {
      // Delete each refresh token
      for (String token : tokens) {
        String refreshKey = REFRESH_PREFIX + token;
        redisTemplate.delete(refreshKey);
      }
      log.info("Redis - Deleted {} refresh tokens for user {}", tokens.size(), userId);
    }

    // Delete the user's refresh token set
    redisTemplate.delete(userRefreshKey);
  }

  @Override
  public boolean refreshTokenExists(String token) {
    String key = REFRESH_PREFIX + token;
    Boolean exists = redisTemplate.hasKey(key);
    return Boolean.TRUE.equals(exists);
  }

  // ===== Temporary Registration Token Methods =====

  private static final String TEMP_REGISTRATION_PREFIX = "temp:registration:";

  @Override
  public void saveTempRegistrationToken(String tempToken, String phoneNumber, long expirationMs) {
    String key = TEMP_REGISTRATION_PREFIX + tempToken;
    redisTemplate.opsForValue().set(key, phoneNumber, expirationMs, TimeUnit.MILLISECONDS);
    log.debug(
        "Redis - Saved temp registration token for phone: {}, TTL: {}ms", phoneNumber, expirationMs);
  }

  @Override
  public String getPhoneByTempToken(String tempToken) {
    String key = TEMP_REGISTRATION_PREFIX + tempToken;
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public void deleteTempToken(String tempToken) {
    String key = TEMP_REGISTRATION_PREFIX + tempToken;
    redisTemplate.delete(key);
    log.debug("Redis - Deleted temp registration token: {}...",
        tempToken.substring(0, Math.min(20, tempToken.length())));
  }
}
