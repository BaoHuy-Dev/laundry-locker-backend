package com.huynqb.laundrylockerbackend.module.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for refund information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refund information")
public class RefundResponse {

  @Schema(description = "Refund ID")
  private Long id;

  @Schema(description = "Original payment ID")
  private Long paymentId;

  @Schema(description = "Order ID")
  private Long orderId;

  @Schema(description = "Refund amount")
  private BigDecimal amount;

  @Schema(description = "Original payment amount")
  private BigDecimal originalAmount;

  @Schema(description = "Refund status", example = "PENDING")
  private String status;

  @Schema(description = "Reason for refund")
  private String reason;

  @Schema(description = "Refund transaction ID from payment gateway")
  private String transactionId;

  @Schema(description = "When refund was requested")
  private LocalDateTime requestedAt;

  @Schema(description = "When refund was processed")
  private LocalDateTime processedAt;

  @Schema(description = "Additional notes")
  private String notes;

  @Schema(description = "Processed by (admin ID)")
  private Long processedBy;
}
