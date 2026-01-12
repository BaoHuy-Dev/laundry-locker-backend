package com.huynqb.laundrylockerbackend.module.auth.service;

/**
 * Token Service Interface Abstraction layer for token storage operations. Can be implemented with
 * Redis, Database, or other storage systems.
 */
public interface TokenService {

  /**
   * Add an access token to the blacklist
   *
   * @param token The access token to blacklist
   * @param expirationMs Time until the token expires (in milliseconds)
   */
  void blacklistAccessToken(String token, long expirationMs);

  /**
   * Check if an access token is blacklisted
   *
   * @param token The access token to check
   * @return true if blacklisted, false otherwise
   */
  boolean isAccessTokenBlacklisted(String token);

  /**
   * Save a refresh token
   *
   * @param token The refresh token
   * @param userId The user ID associated with this token
   * @param expirationMs Time until the token expires (in milliseconds)
   */
  void saveRefreshToken(String token, Long userId, long expirationMs);

  /**
   * Get user ID by refresh token
   *
   * @param token The refresh token
   * @return The user ID, or null if not found
   */
  Long getUserIdByRefreshToken(String token);

  /**
   * Delete a refresh token
   *
   * @param token The refresh token to delete
   */
  void deleteRefreshToken(String token);

  /**
   * Delete all refresh tokens for a user (force logout from all devices)
   *
   * @param userId The user ID
   */
  void deleteAllUserRefreshTokens(Long userId);

  /**
   * Check if a refresh token exists
   *
   * @param token The refresh token
   * @return true if exists, false otherwise
   */
  boolean refreshTokenExists(String token);
}
