package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for system health information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "System health status")
public class SystemHealthResponse {

  @Schema(description = "Overall system status: UP, DOWN, DEGRADED")
  private String status;

  @Schema(description = "Server timestamp")
  private LocalDateTime timestamp;

  @Schema(description = "System uptime in seconds")
  private Long uptimeSeconds;

  @Schema(description = "Formatted uptime (e.g., '5d 3h 20m')")
  private String uptimeFormatted;

  @Schema(description = "Application version")
  private String version;

  @Schema(description = "Database health")
  private ComponentHealth database;

  @Schema(description = "Cache (Redis) health")
  private ComponentHealth cache;

  @Schema(description = "Message queue health")
  private ComponentHealth messageQueue;

  @Schema(description = "External services health")
  private Map<String, ComponentHealth> externalServices;

  @Schema(description = "Memory usage")
  private MemoryInfo memory;

  @Schema(description = "Disk usage")
  private DiskInfo disk;

  @Schema(description = "CPU usage percentage")
  private Double cpuUsage;

  @Schema(description = "Active connections count")
  private Integer activeConnections;

  @Schema(description = "Active sessions count")
  private Integer activeSessions;

  @Schema(description = "Pending tasks count")
  private Integer pendingTasks;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Component health status")
  public static class ComponentHealth {
    @Schema(description = "Component status: UP, DOWN, UNKNOWN")
    private String status;

    @Schema(description = "Response time in milliseconds")
    private Long responseTimeMs;

    @Schema(description = "Additional details")
    private String details;

    @Schema(description = "Last check timestamp")
    private LocalDateTime lastCheck;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Memory usage information")
  public static class MemoryInfo {
    @Schema(description = "Used memory in bytes")
    private Long usedBytes;

    @Schema(description = "Free memory in bytes")
    private Long freeBytes;

    @Schema(description = "Total memory in bytes")
    private Long totalBytes;

    @Schema(description = "Max memory in bytes")
    private Long maxBytes;

    @Schema(description = "Usage percentage")
    private Double usagePercent;

    @Schema(description = "Formatted used memory (e.g., '512 MB')")
    private String usedFormatted;

    @Schema(description = "Formatted total memory")
    private String totalFormatted;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Disk usage information")
  public static class DiskInfo {
    @Schema(description = "Used disk space in bytes")
    private Long usedBytes;

    @Schema(description = "Free disk space in bytes")
    private Long freeBytes;

    @Schema(description = "Total disk space in bytes")
    private Long totalBytes;

    @Schema(description = "Usage percentage")
    private Double usagePercent;

    @Schema(description = "Formatted used space")
    private String usedFormatted;

    @Schema(description = "Formatted free space")
    private String freeFormatted;
  }
}
