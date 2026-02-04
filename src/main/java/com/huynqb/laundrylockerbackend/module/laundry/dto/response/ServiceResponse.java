package com.huynqb.laundrylockerbackend.module.laundry.dto.response;

import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceCategory;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceStatus;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for LaundryService. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

  private Long id;
  private String name;
  private String image;
  private BigDecimal price;
  private BigDecimal maxPrice;
  private String unit;
  private String description;
  private ServiceStatus status;
  private ServiceCategory category;
  private ServiceType serviceType;
  private Boolean isAddon;
  private Boolean isMonthlyPackage;
  private Integer estimatedHours;
  private Long storeId;
  private String storeName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
