package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.SystemHealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for system health monitoring (Admin only). */
@Tag(name = TagConstants.ROOT_TAG_ADMIN_SYSTEM, description = "System health and monitoring APIs")
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN)
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminSystemController {

  private final DataSource dataSource;
  private final ResponseHelper responseHelper;

  private static final long START_TIME = System.currentTimeMillis();

  /** Get system health status. */
  @Operation(summary = "Get System Health", description = "Get comprehensive system health status")
  @GetMapping(UriParamConstants.ADMIN_SYSTEM_HEALTH)
  public ResponseEntity<ApiResponse<SystemHealthResponse>> getSystemHealth() {
    SystemHealthResponse health = buildHealthResponse();
    return ResponseEntity.ok(responseHelper.success(health, "SYSTEM_HEALTH_RETRIEVED"));
  }

  private SystemHealthResponse buildHealthResponse() {
    // Calculate uptime
    long uptimeSeconds = (System.currentTimeMillis() - START_TIME) / 1000;

    // Check database
    SystemHealthResponse.ComponentHealth dbHealth = checkDatabase();

    // Get memory info
    SystemHealthResponse.MemoryInfo memoryInfo = getMemoryInfo();

    // Get disk info
    SystemHealthResponse.DiskInfo diskInfo = getDiskInfo();

    // Get CPU usage
    double cpuUsage = getCpuUsage();

    // Determine overall status
    String overallStatus = determineOverallStatus(dbHealth);

    return SystemHealthResponse.builder()
        .status(overallStatus)
        .timestamp(LocalDateTime.now())
        .uptimeSeconds(uptimeSeconds)
        .uptimeFormatted(formatUptime(uptimeSeconds))
        .version(getApplicationVersion())
        .database(dbHealth)
        .memory(memoryInfo)
        .disk(diskInfo)
        .cpuUsage(cpuUsage)
        .externalServices(checkExternalServices())
        .build();
  }

  private SystemHealthResponse.ComponentHealth checkDatabase() {
    long startTime = System.currentTimeMillis();
    try (Connection conn = dataSource.getConnection()) {
      long responseTime = System.currentTimeMillis() - startTime;
      return SystemHealthResponse.ComponentHealth.builder()
          .status("UP")
          .responseTimeMs(responseTime)
          .details("Database connection successful")
          .lastCheck(LocalDateTime.now())
          .build();
    } catch (Exception e) {
      log.error("Database health check failed", e);
      return SystemHealthResponse.ComponentHealth.builder()
          .status("DOWN")
          .responseTimeMs(System.currentTimeMillis() - startTime)
          .details("Database connection failed: " + e.getMessage())
          .lastCheck(LocalDateTime.now())
          .build();
    }
  }

  private SystemHealthResponse.MemoryInfo getMemoryInfo() {
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

    long used = heapUsage.getUsed();
    long max = heapUsage.getMax();
    long committed = heapUsage.getCommitted();
    double usagePercent = max > 0 ? (double) used / max * 100 : 0;

    return SystemHealthResponse.MemoryInfo.builder()
        .usedBytes(used)
        .freeBytes(max - used)
        .totalBytes(committed)
        .maxBytes(max)
        .usagePercent(Math.round(usagePercent * 100.0) / 100.0)
        .usedFormatted(formatBytes(used))
        .totalFormatted(formatBytes(max))
        .build();
  }

  private SystemHealthResponse.DiskInfo getDiskInfo() {
    java.io.File root = new java.io.File("/");
    long total = root.getTotalSpace();
    long free = root.getFreeSpace();
    long used = total - free;
    double usagePercent = total > 0 ? (double) used / total * 100 : 0;

    return SystemHealthResponse.DiskInfo.builder()
        .usedBytes(used)
        .freeBytes(free)
        .totalBytes(total)
        .usagePercent(Math.round(usagePercent * 100.0) / 100.0)
        .usedFormatted(formatBytes(used))
        .freeFormatted(formatBytes(free))
        .build();
  }

  private double getCpuUsage() {
    try {
      com.sun.management.OperatingSystemMXBean osBean =
          (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      double cpuLoad = osBean.getProcessCpuLoad() * 100;
      return Math.round(cpuLoad * 100.0) / 100.0;
    } catch (Exception e) {
      return -1;
    }
  }

  private Map<String, SystemHealthResponse.ComponentHealth> checkExternalServices() {
    Map<String, SystemHealthResponse.ComponentHealth> services = new HashMap<>();

    // Add checks for external services (Firebase, Payment gateways, etc.)
    // For now, return empty or mock data

    return services;
  }

  private String determineOverallStatus(SystemHealthResponse.ComponentHealth dbHealth) {
    if ("DOWN".equals(dbHealth.getStatus())) {
      return "DOWN";
    }
    if (dbHealth.getResponseTimeMs() > 1000) {
      return "DEGRADED";
    }
    return "UP";
  }

  private String formatUptime(long seconds) {
    long days = seconds / 86400;
    long hours = (seconds % 86400) / 3600;
    long minutes = (seconds % 3600) / 60;

    StringBuilder sb = new StringBuilder();
    if (days > 0) sb.append(days).append("d ");
    if (hours > 0) sb.append(hours).append("h ");
    sb.append(minutes).append("m");
    return sb.toString().trim();
  }

  private String formatBytes(long bytes) {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
    if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
    return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
  }

  private String getApplicationVersion() {
    // Return application version from properties or manifest
    return "1.0.0";
  }
}
