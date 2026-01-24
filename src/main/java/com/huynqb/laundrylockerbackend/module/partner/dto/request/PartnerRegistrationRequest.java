package com.huynqb.laundrylockerbackend.module.partner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for partner registration. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRegistrationRequest {

  @NotBlank(message = "Business name is required")
  @Size(min = 3, max = 200, message = "Business name must be between 3 and 200 characters")
  private String businessName;

  private String businessRegistrationNumber;

  private String taxId;

  @NotBlank(message = "Business address is required")
  private String businessAddress;

  @NotBlank(message = "Contact phone is required")
  private String contactPhone;

  @Email(message = "Invalid email format")
  private String contactEmail;

  private String notes;
}
