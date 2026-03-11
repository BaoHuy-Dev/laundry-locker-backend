package com.huynqb.laundrylockerbackend.module.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for order complaint. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order complaint information")
public class OrderComplaintResponse {

  @Schema(description = "Complaint ID")
  private Long id;

  @Schema(description = "Order ID")
  private Long orderId;

  @Schema(description = "Order code")
  private String orderCode;

  @Schema(description = "User ID")
  private Long userId;

  @Schema(description = "User name")
  private String userName;

  @Schema(description = "Complaint type")
  private String type;

  @Schema(description = "Description of the issue")
  private String description;

  @Schema(description = "Evidence image URLs")
  private List<String> imageUrls;

  @Schema(description = "Complaint status")
  private String status;

  @Schema(description = "Resolution message from admin/partner")
  private String resolution;

  @Schema(description = "When complaint was created")
  private LocalDateTime createdAt;

  @Schema(description = "When complaint was resolved")
  private LocalDateTime resolvedAt;
}
