package com.huynqb.laundrylockerbackend.module.order.dto.request;

import com.huynqb.laundrylockerbackend.module.order.enums.ComplaintType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for creating an order complaint. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a complaint about an order")
public class OrderComplaintRequest {

  @NotNull(message = "Complaint type is required")
  @Schema(description = "Type of complaint", example = "DAMAGED")
  private ComplaintType type;

  @NotBlank(message = "Description is required")
  @Size(max = 1000, message = "Description must not exceed 1000 characters")
  @Schema(
      description = "Detailed description of the issue",
      example = "My shirt has a stain after washing")
  private String description;

  @Schema(description = "URLs of evidence images (optional)")
  private List<String> imageUrls;
}
