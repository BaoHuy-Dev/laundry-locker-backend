package com.huynqb.laundrylockerbackend.module.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for rating an order. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to rate a completed order")
public class OrderRatingRequest {

  @NotNull(message = "Rating is required")
  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must be at most 5")
  @Schema(description = "Rating from 1 to 5 stars", example = "5")
  private Integer rating;

  @Size(max = 500, message = "Comment must not exceed 500 characters")
  @Schema(description = "Optional review comment", example = "Great service, very clean!")
  private String comment;

  @Schema(description = "Rating for service quality (1-5)", example = "5")
  @Min(value = 1, message = "Service rating must be at least 1")
  @Max(value = 5, message = "Service rating must be at most 5")
  private Integer serviceRating;

  @Schema(description = "Rating for delivery speed (1-5)", example = "4")
  @Min(value = 1, message = "Speed rating must be at least 1")
  @Max(value = 5, message = "Speed rating must be at most 5")
  private Integer speedRating;

  @Schema(description = "Rating for staff friendliness (1-5)", example = "5")
  @Min(value = 1, message = "Staff rating must be at least 1")
  @Max(value = 5, message = "Staff rating must be at most 5")
  private Integer staffRating;
}
