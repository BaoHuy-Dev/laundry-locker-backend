package com.huynqb.laundrylockerbackend.module.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for nearby store information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nearby store information")
public class NearbyStoreResponse {

  @Schema(description = "Store ID")
  private Long id;

  @Schema(description = "Store name")
  private String name;

  @Schema(description = "Store address")
  private String address;

  @Schema(description = "Store phone number")
  private String phone;

  @Schema(description = "Store latitude")
  private Double latitude;

  @Schema(description = "Store longitude")
  private Double longitude;

  @Schema(description = "Distance from current location in meters")
  private Double distanceMeters;

  @Schema(description = "Formatted distance (e.g., '1.2 km', '500 m')")
  private String distanceFormatted;

  @Schema(description = "Average rating (1-5)")
  private Double averageRating;

  @Schema(description = "Total number of reviews")
  private Integer reviewCount;

  @Schema(description = "Whether the store is currently open")
  private Boolean isOpen;

  @Schema(description = "Store operating hours")
  private String operatingHours;

  @Schema(description = "Number of available lockers")
  private Integer availableLockers;

  @Schema(description = "Total number of lockers")
  private Integer totalLockers;

  @Schema(description = "Store image URL")
  private String imageUrl;

  @Schema(description = "Store status")
  private Boolean isActive;

  @Schema(description = "Partner ID")
  private Long partnerId;

  @Schema(description = "Partner name")
  private String partnerName;
}
