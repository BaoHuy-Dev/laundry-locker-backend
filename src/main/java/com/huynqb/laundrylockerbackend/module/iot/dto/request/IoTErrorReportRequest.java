package com.huynqb.laundrylockerbackend.module.iot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for reporting IoT device errors. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IoT device error report request")
public class IoTErrorReportRequest {

  @NotNull(message = "Device ID is required")
  @Schema(description = "Device ID reporting the error")
  private Long deviceId;

  @NotBlank(message = "Error code is required")
  @Schema(description = "Error code", example = "ERR_DOOR_SENSOR")
  private String errorCode;

  @NotBlank(message = "Error message is required")
  @Schema(description = "Error message", example = "Door sensor not responding")
  private String message;

  @Schema(description = "Error severity: INFO, WARNING, ERROR, CRITICAL")
  @Builder.Default
  private ErrorSeverity severity = ErrorSeverity.ERROR;

  @Schema(description = "When the error occurred")
  private LocalDateTime occurredAt;

  @Schema(description = "Component that caused the error", example = "DOOR_SENSOR")
  private String component;

  @Schema(description = "Stack trace or additional technical details")
  private String stackTrace;

  @Schema(description = "Device state at time of error")
  private Map<String, Object> deviceState;

  @Schema(description = "Suggested resolution if known")
  private String suggestedResolution;

  @Schema(description = "Whether the error is recoverable", example = "true")
  @Builder.Default
  private Boolean recoverable = true;

  @Schema(description = "Error count (if same error repeated)")
  @Builder.Default
  private Integer errorCount = 1;

  @Schema(description = "Firmware version at time of error")
  private String firmwareVersion;

  public enum ErrorSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
  }
}
