package com.huynqb.laundrylockerbackend.module.iot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for IoT device health status. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IoT device health status")
public class IoTHealthResponse {

  @Schema(description = "Device ID")
  private Long deviceId;

  @Schema(description = "Device code/identifier")
  private String deviceCode;

  @Schema(description = "Device type: LOCKER, SENSOR, CAMERA, SCALE")
  private String deviceType;

  @Schema(description = "Overall health status: HEALTHY, WARNING, CRITICAL, OFFLINE")
  private String status;

  @Schema(description = "Last heartbeat timestamp")
  private LocalDateTime lastHeartbeat;

  @Schema(description = "Seconds since last heartbeat")
  private Long secondsSinceHeartbeat;

  @Schema(description = "Is device online")
  private Boolean isOnline;

  @Schema(description = "Firmware version")
  private String firmwareVersion;

  @Schema(description = "Is firmware up to date")
  private Boolean firmwareUpToDate;

  @Schema(description = "Latest available firmware version")
  private String latestFirmwareVersion;

  @Schema(description = "Battery level percentage (for battery-powered devices)")
  private Integer batteryLevel;

  @Schema(description = "Signal strength (RSSI)")
  private Integer signalStrength;

  @Schema(description = "WiFi network name")
  private String wifiNetwork;

  @Schema(description = "Device temperature in Celsius")
  private Double temperature;

  @Schema(description = "Device uptime in seconds")
  private Long uptimeSeconds;

  @Schema(description = "Memory usage percentage")
  private Double memoryUsage;

  @Schema(description = "Storage usage percentage")
  private Double storageUsage;

  @Schema(description = "Error count in last 24 hours")
  private Integer errorCount24h;

  @Schema(description = "Recent errors")
  private java.util.List<RecentError> recentErrors;

  @Schema(description = "Diagnostic metrics")
  private Map<String, Object> diagnostics;

  @Schema(description = "Store ID")
  private Long storeId;

  @Schema(description = "Store name")
  private String storeName;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Recent error information")
  public static class RecentError {
    @Schema(description = "Error code")
    private String errorCode;

    @Schema(description = "Error message")
    private String message;

    @Schema(description = "When the error occurred")
    private LocalDateTime occurredAt;

    @Schema(description = "Error severity: INFO, WARNING, ERROR, CRITICAL")
    private String severity;

    @Schema(description = "Is error resolved")
    private Boolean resolved;
  }
}
