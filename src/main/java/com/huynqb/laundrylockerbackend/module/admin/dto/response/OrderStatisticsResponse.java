package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for order statistics. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {

  private Long totalOrders;
  private Long completedOrders;
  private Long canceledOrders;
  private Long pendingOrders;

  private BigDecimal totalRevenue;
  private BigDecimal averageOrderValue;

  private Long ordersToday;
  private Long ordersThisWeek;
  private Long ordersThisMonth;
}
