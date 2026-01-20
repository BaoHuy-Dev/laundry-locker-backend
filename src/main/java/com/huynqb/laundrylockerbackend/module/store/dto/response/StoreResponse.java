package com.huynqb.laundrylockerbackend.module.store.dto.response;

import com.huynqb.laundrylockerbackend.module.store.enums.StoreStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for Store. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

  private Long id;
  private String name;
  private String contactPhone;
  private StoreStatus status;
  private String address;
  private Double longitude;
  private Double latitude;
  private String image;
  private String description;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
