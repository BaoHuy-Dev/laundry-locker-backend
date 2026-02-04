package com.huynqb.laundrylockerbackend.module.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for order timeline event. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single event in the order timeline")
public class OrderTimelineEvent {

  @Schema(description = "Event status code", example = "CONFIRMED")
  private String status;

  @Schema(description = "Human-readable status title", example = "Order Confirmed")
  private String title;

  @Schema(
      description = "Detailed description of the event",
      example = "Customer confirmed items are in locker")
  private String description;

  @Schema(description = "When this event occurred")
  private LocalDateTime timestamp;

  @Schema(description = "Icon name for UI display", example = "check-circle")
  private String icon;

  @Schema(description = "Color code for UI display", example = "#27ae60")
  private String color;

  @Schema(description = "Actor who triggered this event", example = "Customer")
  private String actor;

  @Schema(description = "Additional metadata")
  private String metadata;
}
