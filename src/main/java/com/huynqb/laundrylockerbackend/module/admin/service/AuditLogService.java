package com.huynqb.laundrylockerbackend.module.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AuditLogResponse;
import com.huynqb.laundrylockerbackend.module.admin.entity.AuditLog;
import com.huynqb.laundrylockerbackend.module.admin.repository.AuditLogRepository;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for audit logging. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  /** Log an audit event asynchronously. */
  @Async
  @Transactional
  public void logAsync(
      AuditLog.AuditAction action,
      String entityType,
      Long entityId,
      Long userId,
      String description,
      HttpServletRequest request) {
    log(action, entityType, entityId, userId, null, description, null, null, request);
  }

  /** Log an audit event synchronously. */
  @Transactional
  public void log(
      AuditLog.AuditAction action,
      String entityType,
      Long entityId,
      Long userId,
      String userRole,
      String description,
      Object oldValues,
      Object newValues,
      HttpServletRequest request) {
    try {
      User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

      AuditLog auditLog =
          AuditLog.builder()
              .action(action)
              .entityType(entityType)
              .entityId(entityId)
              .user(user)
              .userRole(userRole)
              .ipAddress(getClientIp(request))
              .userAgent(request != null ? request.getHeader("User-Agent") : null)
              .oldValues(toJson(oldValues))
              .newValues(toJson(newValues))
              .description(description)
              .timestamp(LocalDateTime.now())
              .status(AuditLog.AuditStatus.SUCCESS)
              .requestId(UUID.randomUUID().toString())
              .build();

      auditLogRepository.save(auditLog);
      log.debug("Audit log created: {} - {} - {}", action, entityType, entityId);
    } catch (Exception e) {
      log.error("Failed to create audit log", e);
    }
  }

  /** Log a failed action. */
  @Transactional
  public void logFailure(
      AuditLog.AuditAction action,
      String entityType,
      Long entityId,
      Long userId,
      String description,
      String errorMessage,
      HttpServletRequest request) {
    try {
      User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

      AuditLog auditLog =
          AuditLog.builder()
              .action(action)
              .entityType(entityType)
              .entityId(entityId)
              .user(user)
              .ipAddress(getClientIp(request))
              .userAgent(request != null ? request.getHeader("User-Agent") : null)
              .description(description)
              .timestamp(LocalDateTime.now())
              .status(AuditLog.AuditStatus.FAILURE)
              .errorMessage(errorMessage)
              .requestId(UUID.randomUUID().toString())
              .build();

      auditLogRepository.save(auditLog);
    } catch (Exception e) {
      log.error("Failed to create audit log for failure", e);
    }
  }

  /** Get audit logs with filters. */
  public Page<AuditLogResponse> getAuditLogs(
      AuditLog.AuditAction action,
      String entityType,
      Long userId,
      AuditLog.AuditStatus status,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Pageable pageable) {
    return auditLogRepository
        .searchAuditLogs(action, entityType, userId, status, startDate, endDate, pageable)
        .map(this::mapToResponse);
  }

  /** Get audit logs for a specific entity. */
  public Page<AuditLogResponse> getEntityAuditLogs(
      String entityType, Long entityId, Pageable pageable) {
    return auditLogRepository
        .findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable)
        .map(this::mapToResponse);
  }

  /** Get audit logs for a user. */
  public Page<AuditLogResponse> getUserAuditLogs(Long userId, Pageable pageable) {
    return auditLogRepository
        .findByUserIdOrderByTimestampDesc(userId, pageable)
        .map(this::mapToResponse);
  }

  /** Get audit logs within a date range. */
  public Page<AuditLogResponse> getAuditLogsByDateRange(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    return auditLogRepository
        .findByDateRange(startDate, endDate, pageable)
        .map(this::mapToResponse);
  }

  /** Get action counts for statistics. */
  public Map<String, Long> getActionCounts(LocalDateTime startDate, LocalDateTime endDate) {
    var results = auditLogRepository.countActionsByType(startDate, endDate);
    return results.stream()
        .collect(
            java.util.stream.Collectors.toMap(
                arr -> ((AuditLog.AuditAction) arr[0]).name(), arr -> (Long) arr[1]));
  }

  // ===== Convenience Logging Methods =====

  public void logLogin(Long userId, HttpServletRequest request) {
    log(
        AuditLog.AuditAction.LOGIN,
        "USER",
        userId,
        userId,
        null,
        "User logged in",
        null,
        null,
        request);
  }

  public void logLogout(Long userId, HttpServletRequest request) {
    log(
        AuditLog.AuditAction.LOGOUT,
        "USER",
        userId,
        userId,
        null,
        "User logged out",
        null,
        null,
        request);
  }

  public void logLoginFailed(String email, String reason, HttpServletRequest request) {
    try {
      AuditLog auditLog =
          AuditLog.builder()
              .action(AuditLog.AuditAction.LOGIN_FAILED)
              .entityType("USER")
              .ipAddress(getClientIp(request))
              .userAgent(request != null ? request.getHeader("User-Agent") : null)
              .description("Login failed for: " + email)
              .timestamp(LocalDateTime.now())
              .status(AuditLog.AuditStatus.FAILURE)
              .errorMessage(reason)
              .requestId(UUID.randomUUID().toString())
              .build();

      auditLogRepository.save(auditLog);
    } catch (Exception e) {
      log.error("Failed to log login failure", e);
    }
  }

  public void logOrderCreated(Long orderId, Long userId, HttpServletRequest request) {
    log(
        AuditLog.AuditAction.ORDER_CREATED,
        "ORDER",
        orderId,
        userId,
        null,
        "Order created: " + orderId,
        null,
        null,
        request);
  }

  public void logPaymentCompleted(Long paymentId, Long userId, HttpServletRequest request) {
    log(
        AuditLog.AuditAction.PAYMENT_COMPLETED,
        "PAYMENT",
        paymentId,
        userId,
        null,
        "Payment completed: " + paymentId,
        null,
        null,
        request);
  }

  // ===== Private Helper Methods =====

  private AuditLogResponse mapToResponse(AuditLog auditLog) {
    AuditLogResponse.UserInfo userInfo = null;
    if (auditLog.getUser() != null) {
      User user = auditLog.getUser();
      String fullName =
          (user.getFirstName() != null ? user.getFirstName() : "")
              + " "
              + (user.getLastName() != null ? user.getLastName() : "");
      userInfo =
          AuditLogResponse.UserInfo.builder()
              .id(user.getId())
              .email(user.getEmail())
              .fullName(fullName.trim())
              .build();
    }

    return AuditLogResponse.builder()
        .id(auditLog.getId())
        .action(auditLog.getAction().name())
        .entityType(auditLog.getEntityType())
        .entityId(auditLog.getEntityId())
        .user(userInfo)
        .userRole(auditLog.getUserRole())
        .ipAddress(auditLog.getIpAddress())
        .description(auditLog.getDescription())
        .timestamp(auditLog.getTimestamp())
        .status(auditLog.getStatus().name())
        .errorMessage(auditLog.getErrorMessage())
        .requestId(auditLog.getRequestId())
        .build();
  }

  private String getClientIp(HttpServletRequest request) {
    if (request == null) return null;

    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private String toJson(Object obj) {
    if (obj == null) return null;
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      log.error("Error serializing to JSON", e);
      return null;
    }
  }
}
