package com.huynqb.laundrylockerbackend.module.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for refund request. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to initiate a refund")
public class RefundRequest {

  @NotNull(message = "Payment ID is required")
  @Schema(description = "Payment ID to refund", example = "1")
  private Long paymentId;

  @Positive(message = "Refund amount must be positive")
  @Schema(description = "Amount to refund (null = full refund)", example = "50000")
  private BigDecimal amount;

  @NotNull(message = "Reason is required")
  @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
  @Schema(description = "Reason for refund", example = "Service not satisfactory, items damaged")
  private String reason;

  @Schema(description = "Optional additional notes")
  private String notes;
}
