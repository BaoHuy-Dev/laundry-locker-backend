package com.huynqb.laundrylockerbackend.module.loyalty.dto.response;

import com.huynqb.laundrylockerbackend.module.loyalty.enums.PointTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for point transaction. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionResponse {

  private Long id;
  private Long userId;
  private Long orderId;
  private PointTransactionType type;
  private Long points;
  private BigDecimal relatedAmount;
  private Long balanceAfter;
  private String description;
  private String referenceId;
  private LocalDateTime createdAt;
}
