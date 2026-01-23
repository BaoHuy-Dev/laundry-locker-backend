package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStoreResponse {
  private Long id;
  private String name;
  private String address;
  private String phone;
  private String imageUrl;
  private Double latitude;
  private Double longitude;
  private String description;
  private String openTime;
  private String closeTime;
  private Boolean active;
  private Integer lockerCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
