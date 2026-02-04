package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for audit log information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit log entry")
public class AuditLogResponse {

  @Schema(description = "Audit log ID")
  private Long id;

  @Schema(description = "Action performed")
  private String action;

  @Schema(description = "Entity type affected")
  private String entityType;

  @Schema(description = "Entity ID affected")
  private Long entityId;

  @Schema(description = "User who performed the action")
  private UserInfo user;

  @Schema(description = "Role of the user at time of action")
  private String userRole;

  @Schema(description = "IP address")
  private String ipAddress;

  @Schema(description = "Description of the action")
  private String description;

  @Schema(description = "When the action occurred")
  private LocalDateTime timestamp;

  @Schema(description = "Status of the action")
  private String status;

  @Schema(description = "Error message if failed")
  private String errorMessage;

  @Schema(description = "Request ID for tracing")
  private String requestId;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "User information in audit log")
  public static class UserInfo {
    @Schema(description = "User ID")
    private Long id;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "User full name")
    private String fullName;
  }
}
