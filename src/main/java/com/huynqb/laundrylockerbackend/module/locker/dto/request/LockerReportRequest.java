package com.huynqb.laundrylockerbackend.module.locker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LockerReportRequest {
  @NotBlank(message = "Description is required")
  private String description;
}
