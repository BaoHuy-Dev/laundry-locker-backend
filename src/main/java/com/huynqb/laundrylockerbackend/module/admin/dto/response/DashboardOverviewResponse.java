package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
  private Long totalUsers;
  private Long totalStores;
  private Long totalLockers;
  private Long totalOrders;
  private Long ordersToday;
  private Long pendingOrders;
  private BigDecimal totalRevenue;
  private BigDecimal revenueToday;
  private Long activeServices;
  private Long availableBoxes;
  private Long occupiedBoxes;
}
