package com.huynqb.laundrylockerbackend.module.admin.repository;

import com.huynqb.laundrylockerbackend.module.admin.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for AuditLog entity. */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  /** Find audit logs by user ID. */
  Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

  /** Find audit logs by action type. */
  Page<AuditLog> findByActionOrderByTimestampDesc(AuditLog.AuditAction action, Pageable pageable);

  /** Find audit logs by entity. */
  Page<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
      String entityType, Long entityId, Pageable pageable);

  /** Find audit logs by entity type. */
  Page<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType, Pageable pageable);

  /** Find audit logs within a time range. */
  @Query(
      "SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
  Page<AuditLog> findByDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  /** Find failed actions. */
  Page<AuditLog> findByStatusOrderByTimestampDesc(AuditLog.AuditStatus status, Pageable pageable);

  /** Search audit logs with multiple filters. */
  @Query(
      "SELECT a FROM AuditLog a WHERE "
          + "(:action IS NULL OR a.action = :action) AND "
          + "(:entityType IS NULL OR a.entityType = :entityType) AND "
          + "(:userId IS NULL OR a.user.id = :userId) AND "
          + "(:status IS NULL OR a.status = :status) AND "
          + "(:startDate IS NULL OR a.timestamp >= :startDate) AND "
          + "(:endDate IS NULL OR a.timestamp <= :endDate) "
          + "ORDER BY a.timestamp DESC")
  Page<AuditLog> searchAuditLogs(
      @Param("action") AuditLog.AuditAction action,
      @Param("entityType") String entityType,
      @Param("userId") Long userId,
      @Param("status") AuditLog.AuditStatus status,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  /** Find login attempts by IP address. */
  @Query(
      "SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress "
          + "AND a.action IN ('LOGIN', 'LOGIN_FAILED') "
          + "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
  List<AuditLog> findLoginAttemptsByIp(
      @Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

  /** Count actions by type within a time range. */
  @Query(
      "SELECT a.action, COUNT(a) FROM AuditLog a "
          + "WHERE a.timestamp BETWEEN :startDate AND :endDate "
          + "GROUP BY a.action")
  List<Object[]> countActionsByType(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  /** Find recent activities for a user. */
  @Query(
      "SELECT a FROM AuditLog a WHERE a.user.id = :userId "
          + "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
  List<AuditLog> findRecentUserActivities(
      @Param("userId") Long userId, @Param("since") LocalDateTime since);

  /** Delete old audit logs (for data retention). */
  @Query("DELETE FROM AuditLog a WHERE a.timestamp < :before")
  void deleteOldLogs(@Param("before") LocalDateTime before);
}
