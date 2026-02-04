package com.huynqb.laundrylockerbackend.module.admin.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for revenue report. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {

  private BigDecimal totalRevenue;
  private BigDecimal revenueToday;
  private BigDecimal revenueThisWeek;
  private BigDecimal revenueThisMonth;

  private Long totalTransactions;
  private Long successfulTransactions;
  private Long failedTransactions;

  /** Revenue breakdown by payment method. */
  private Map<String, BigDecimal> revenueByPaymentMethod;

  /** Daily revenue for chart. */
  private List<DailyRevenueEntry> dailyRevenue;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DailyRevenueEntry {
    private LocalDate date;
    private BigDecimal revenue;
    private Long orderCount;
  }
}
