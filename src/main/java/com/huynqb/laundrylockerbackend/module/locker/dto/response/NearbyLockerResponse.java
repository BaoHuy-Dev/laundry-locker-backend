package com.huynqb.laundrylockerbackend.module.locker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for nearby locker information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nearby locker information")
public class NearbyLockerResponse {

  @Schema(description = "Locker ID")
  private Long id;

  @Schema(description = "Locker code")
  private String lockerCode;

  @Schema(description = "Locker size: SMALL, MEDIUM, LARGE")
  private String size;

  @Schema(description = "Locker status: AVAILABLE, IN_USE, MAINTENANCE")
  private String status;

  @Schema(description = "Store ID")
  private Long storeId;

  @Schema(description = "Store name")
  private String storeName;

  @Schema(description = "Store address")
  private String storeAddress;

  @Schema(description = "Locker latitude")
  private Double latitude;

  @Schema(description = "Locker longitude")
  private Double longitude;

  @Schema(description = "Distance from current location in meters")
  private Double distanceMeters;

  @Schema(description = "Formatted distance (e.g., '1.2 km', '500 m')")
  private String distanceFormatted;

  @Schema(description = "Base price per day")
  private BigDecimal pricePerDay;

  @Schema(description = "Price per hour (for short-term)")
  private BigDecimal pricePerHour;

  @Schema(description = "Whether the locker is available")
  private Boolean isAvailable;

  @Schema(description = "Store operating hours")
  private String operatingHours;

  @Schema(description = "Whether the store is currently open")
  private Boolean isStoreOpen;
}
