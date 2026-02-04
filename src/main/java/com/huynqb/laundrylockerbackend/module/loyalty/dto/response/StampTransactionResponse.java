package com.huynqb.laundrylockerbackend.module.loyalty.dto.response;

import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for stamp transaction. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampTransactionResponse {

  private Long id;
  private Long userId;
  private Long stampCardId;
  private Long orderId;
  private StampTransactionType type;
  private Integer stamps;
  private Integer stampsAfter;
  private Integer rewardsAfter;
  private BigDecimal discountApplied;
  private String description;
  private LocalDateTime createdAt;
}
