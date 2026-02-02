package com.huynqb.laundrylockerbackend.module.iot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating IoT device configuration. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IoT device configuration update request")
public class IoTConfigRequest {

  @NotNull(message = "Device ID is required")
  @Schema(description = "Device ID to configure")
  private Long deviceId;

  @Schema(description = "Heartbeat interval in seconds", example = "60")
  private Integer heartbeatIntervalSeconds;

  @Schema(description = "Auto-lock timeout in seconds", example = "300")
  private Integer autoLockTimeoutSeconds;

  @Schema(description = "Temperature threshold for alerts (Celsius)", example = "45")
  private Double temperatureThreshold;

  @Schema(description = "Battery low threshold percentage", example = "20")
  private Integer batteryLowThreshold;

  @Schema(description = "Enable/disable device sounds", example = "true")
  private Boolean soundEnabled;

  @Schema(description = "Sound volume level (0-100)", example = "50")
  private Integer volumeLevel;

  @Schema(description = "LED brightness (0-100)", example = "80")
  private Integer ledBrightness;

  @Schema(description = "WiFi SSID")
  private String wifiSsid;

  @Schema(description = "WiFi password (encrypted)")
  private String wifiPassword;

  @Schema(description = "Enable/disable OTA updates", example = "true")
  private Boolean otaEnabled;

  @Schema(description = "Maintenance mode", example = "false")
  private Boolean maintenanceMode;

  @Schema(description = "Log level: DEBUG, INFO, WARNING, ERROR")
  private String logLevel;

  @Schema(description = "Custom configuration parameters")
  private Map<String, Object> customConfig;

  @Schema(description = "Restart device after config update", example = "false")
  @Builder.Default
  private Boolean restartDevice = false;
}
