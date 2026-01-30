package com.huynqb.laundrylockerbackend.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating image URLs. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateImageRequest {

  @NotBlank(message = "Image URL is required")
  @Size(max = 1000, message = "Image URL must be at most 1000 characters")
  private String imageUrl;
}
