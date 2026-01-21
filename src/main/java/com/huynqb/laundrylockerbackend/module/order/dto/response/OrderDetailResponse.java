package com.huynqb.laundrylockerbackend.module.order.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for order detail. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

  private Long id;
  private Long serviceId;
  private String serviceName;
  private String serviceImage;
  private Double quantity;
  private String unit;
  private BigDecimal price;
  private String description;
}
