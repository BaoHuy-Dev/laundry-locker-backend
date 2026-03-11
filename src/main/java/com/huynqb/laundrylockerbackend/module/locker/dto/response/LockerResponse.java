package com.huynqb.laundrylockerbackend.module.locker.dto.response;

import com.huynqb.laundrylockerbackend.module.locker.enums.LockerStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for Locker. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockerResponse {

  private Long id;
  private String code;
  private String name;
  private LockerStatus status;
  private String address;
  private Double longitude;
  private Double latitude;
  private String description;
  private Long storeId;
  private String storeName;
  private Integer totalBoxes;
  private Integer availableBoxes;
  private List<BoxResponse> boxes;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
