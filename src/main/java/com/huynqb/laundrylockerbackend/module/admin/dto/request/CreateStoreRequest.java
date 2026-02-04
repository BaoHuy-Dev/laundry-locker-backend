package com.huynqb.laundrylockerbackend.module.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreRequest {
  @NotBlank(message = "Store name is required")
  private String name;

  private String address;
  private String phone;
  private String imageUrl;
  private Double latitude;
  private Double longitude;
  private String description;
  private String openTime;
  private String closeTime;
}
