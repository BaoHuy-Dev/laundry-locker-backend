package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AuditLogResponse;
import com.huynqb.laundrylockerbackend.module.admin.entity.AuditLog;
import com.huynqb.laundrylockerbackend.module.admin.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for viewing audit logs (Admin only). */
@Tag(name = TagConstants.ROOT_TAG_ADMIN_AUDIT_LOGS, description = "Audit log viewing APIs")
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN + UriParamConstants.ADMIN_AUDIT_LOGS)
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

  private final AuditLogService auditLogService;
  private final ResponseHelper responseHelper;

  /** Get audit logs with filters. */
  @Operation(summary = "Get Audit Logs", description = "Get audit logs with optional filters")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
      @RequestParam(required = false) AuditLog.AuditAction action,
      @RequestParam(required = false) String entityType,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) AuditLog.AuditStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endDate,
      Pageable pageable) {
    Page<AuditLogResponse> logs =
        auditLogService.getAuditLogs(
            action, entityType, userId, status, startDate, endDate, pageable);
    return ResponseEntity.ok(responseHelper.success(logs, "AUDIT_LOGS_RETRIEVED"));
  }

  /** Get audit logs for a specific entity. */
  @Operation(
      summary = "Get Entity Audit Logs",
      description = "Get audit logs for a specific entity")
  @GetMapping(UriParamConstants.AUDIT_LOG_ENTITY)
  public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getEntityAuditLogs(
      @PathVariable String entityType, @PathVariable Long entityId, Pageable pageable) {
    Page<AuditLogResponse> logs =
        auditLogService.getEntityAuditLogs(entityType, entityId, pageable);
    return ResponseEntity.ok(responseHelper.success(logs, "AUDIT_LOGS_RETRIEVED"));
  }

  /** Get audit logs for a specific user. */
  @Operation(summary = "Get User Audit Logs", description = "Get audit logs for a specific user")
  @GetMapping(UriParamConstants.AUDIT_LOG_USER)
  public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getUserAuditLogs(
      @PathVariable Long userId, Pageable pageable) {
    Page<AuditLogResponse> logs = auditLogService.getUserAuditLogs(userId, pageable);
    return ResponseEntity.ok(responseHelper.success(logs, "AUDIT_LOGS_RETRIEVED"));
  }

  /** Get action statistics. */
  @Operation(
      summary = "Get Action Statistics",
      description = "Get count of actions within a date range")
  @GetMapping(UriParamConstants.AUDIT_LOG_STATISTICS)
  public ResponseEntity<ApiResponse<Map<String, Long>>> getActionStatistics(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
    Map<String, Long> stats = auditLogService.getActionCounts(startDate, endDate);
    return ResponseEntity.ok(responseHelper.success(stats, "STATISTICS_RETRIEVED"));
  }
}
