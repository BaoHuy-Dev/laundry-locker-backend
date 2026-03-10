package com.huynqb.laundrylockerbackend.module.user.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatisticsResponse {
  private long totalLaundryOrders;
  private long totalStorageOrders;
  private BigDecimal totalAmountSpent;
  private long totalVouchersUsed;
}
