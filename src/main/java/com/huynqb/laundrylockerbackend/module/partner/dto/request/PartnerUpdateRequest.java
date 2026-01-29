package com.huynqb.laundrylockerbackend.module.partner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating partner profile. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerUpdateRequest {

  @Size(min = 3, max = 200, message = "Business name must be between 3 and 200 characters")
  private String businessName;

  private String businessAddress;

  private String contactPhone;

  @Email(message = "Invalid email format")
  private String contactEmail;

  private String notes;
}
