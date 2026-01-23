package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.enums.LockerStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLockerResponse {
  private Long id;
  private String code;
  private String name;
  private String address;
  private Long storeId;
  private String storeName;
  private LockerStatus status;
  private Integer totalBoxes;
  private Integer availableBoxes;
  private List<BoxInfo> boxes;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BoxInfo {
    private Long id;
    private Integer boxNumber;
    private BoxStatus status;
    private String description;
  }
}
