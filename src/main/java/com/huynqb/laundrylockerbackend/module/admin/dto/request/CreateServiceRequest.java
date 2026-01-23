package com.huynqb.laundrylockerbackend.module.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {
  @NotBlank(message = "Service name is required")
  private String name;

  private String description;

  @NotNull(message = "Price is required")
  private BigDecimal price;

  private String unit;
  private String imageUrl;
  private Integer estimatedMinutes;
  private Long storeId; // null = available at all stores
}
