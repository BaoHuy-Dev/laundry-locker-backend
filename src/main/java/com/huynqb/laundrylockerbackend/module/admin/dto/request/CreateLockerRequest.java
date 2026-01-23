package com.huynqb.laundrylockerbackend.module.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLockerRequest {
  @NotBlank(message = "Locker code is required")
  private String code;

  @NotBlank(message = "Locker name is required")
  private String name;

  private String address;

  @NotNull(message = "Store ID is required")
  private Long storeId;
}
