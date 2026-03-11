package com.huynqb.laundrylockerbackend.module.admin.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity for audit logging - tracks all important system actions. */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
      @Index(name = "idx_audit_action", columnList = "action"),
      @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
      @Index(name = "idx_audit_user", columnList = "user_id"),
      @Index(name = "idx_audit_timestamp", columnList = "timestamp")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false, length = 50)
  private AuditAction action;

  @Column(name = "entity_type", nullable = false, length = 50)
  private String entityType;

  @Column(name = "entity_id")
  private Long entityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "user_role", length = 30)
  private String userRole;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "old_values", columnDefinition = "TEXT")
  private String oldValues; // JSON

  @Column(name = "new_values", columnDefinition = "TEXT")
  private String newValues; // JSON

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20)
  @Builder.Default
  private AuditStatus status = AuditStatus.SUCCESS;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  @Column(name = "request_id", length = 100)
  private String requestId;

  @Column(name = "session_id", length = 100)
  private String sessionId;

  public enum AuditAction {
    // Auth actions
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    PASSWORD_RESET,
    PASSWORD_CHANGED,

    // CRUD actions
    CREATE,
    READ,
    UPDATE,
    DELETE,

    // Order actions
    ORDER_CREATED,
    ORDER_UPDATED,
    ORDER_CANCELLED,
    ORDER_COMPLETED,
    ORDER_STATUS_CHANGED, // Consolidated from order_status_history

    // Payment actions
    PAYMENT_INITIATED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    REFUND_REQUESTED,
    REFUND_PROCESSED,

    // Admin actions
    USER_SUSPENDED,
    USER_ACTIVATED,
    PARTNER_APPROVED,
    PARTNER_REJECTED,
    PROMOTION_CREATED,
    PROMOTION_UPDATED,
    SETTINGS_CHANGED,

    // IoT Device actions (consolidated from iot_device_errors table)
    IOT_ERROR,
    IOT_WARNING,
    IOT_INFO,
    IOT_DEVICE_ONLINE,
    IOT_DEVICE_OFFLINE,
    IOT_LOCK_OPENED,
    IOT_LOCK_CLOSED,

    // System actions
    EXPORT_DATA,
    IMPORT_DATA,
    SYSTEM_CONFIG_CHANGED,
    MAINTENANCE_MODE
  }

  public enum AuditStatus {
    SUCCESS,
    FAILURE,
    PARTIAL
  }

  @PrePersist
  protected void onCreate() {
    if (timestamp == null) {
      timestamp = LocalDateTime.now();
    }
  }
}
