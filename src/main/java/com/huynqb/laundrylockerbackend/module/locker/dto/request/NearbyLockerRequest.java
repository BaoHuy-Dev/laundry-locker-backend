package com.huynqb.laundrylockerbackend.module.locker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for searching nearby lockers. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nearby lockers search request")
public class NearbyLockerRequest {

  @NotNull(message = "Latitude is required")
  @Min(value = -90, message = "Latitude must be between -90 and 90")
  @Max(value = 90, message = "Latitude must be between -90 and 90")
  @Schema(description = "Current latitude", example = "10.7769")
  private Double latitude;

  @NotNull(message = "Longitude is required")
  @Min(value = -180, message = "Longitude must be between -180 and 180")
  @Max(value = 180, message = "Longitude must be between -180 and 180")
  @Schema(description = "Current longitude", example = "106.7009")
  private Double longitude;

  @Min(value = 100, message = "Radius must be at least 100 meters")
  @Max(value = 50000, message = "Radius cannot exceed 50000 meters (50km)")
  @Schema(description = "Search radius in meters", example = "3000", defaultValue = "3000")
  @Builder.Default
  private Integer radiusMeters = 3000;

  @Schema(description = "Filter by locker size: SMALL, MEDIUM, LARGE")
  private String size;

  @Schema(description = "Filter by availability", example = "true")
  @Builder.Default
  private Boolean availableOnly = true;

  @Schema(description = "Sort by: 'distance', 'price', 'rating'", example = "distance")
  @Builder.Default
  private String sortBy = "distance";

  @Schema(description = "Maximum number of results", example = "20")
  @Builder.Default
  private Integer limit = 20;
}
