package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminServiceResponse {
  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private String unit;
  private String imageUrl;
  private Integer estimatedMinutes;
  private Long storeId;
  private String storeName;
  private Boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
