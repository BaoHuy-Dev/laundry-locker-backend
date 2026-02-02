package com.huynqb.laundrylockerbackend.module.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for complete order timeline. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete order timeline showing all status changes")
public class OrderTimelineResponse {

  @Schema(description = "Order ID")
  private Long orderId;

  @Schema(description = "Current order status")
  private String currentStatus;

  @Schema(description = "Estimated completion time")
  private String estimatedCompletion;

  @Schema(description = "Progress percentage (0-100)")
  private Integer progressPercentage;

  @Schema(description = "List of timeline events in chronological order")
  private List<OrderTimelineEvent> events;

  @Schema(description = "Next expected action")
  private String nextAction;

  @Schema(description = "Who should perform the next action")
  private String nextActionActor;
}
