package com.huynqb.laundrylockerbackend.module.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for order rating. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order rating information")
public class OrderRatingResponse {

  @Schema(description = "Rating ID")
  private Long id;

  @Schema(description = "Order ID")
  private Long orderId;

  @Schema(description = "User ID who made the rating")
  private Long userId;

  @Schema(description = "User name")
  private String userName;

  @Schema(description = "User avatar URL")
  private String userAvatar;

  @Schema(description = "Overall rating (1-5)")
  private Integer rating;

  @Schema(description = "Review comment")
  private String comment;

  @Schema(description = "Service quality rating (1-5)")
  private Integer serviceRating;

  @Schema(description = "Speed rating (1-5)")
  private Integer speedRating;

  @Schema(description = "Staff rating (1-5)")
  private Integer staffRating;

  @Schema(description = "Partner/Store response to the rating")
  private String partnerResponse;

  @Schema(description = "When the rating was created")
  private LocalDateTime createdAt;

  @Schema(description = "When partner responded")
  private LocalDateTime respondedAt;
}
