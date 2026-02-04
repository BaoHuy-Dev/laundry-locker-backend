package com.huynqb.laundrylockerbackend.module.partner.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for partner revenue report. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRevenueResponse {

  private Long partnerId;
  private String businessName;

  /** Revenue period */
  private LocalDateTime fromDate;

  private LocalDateTime toDate;

  /** Revenue statistics */
  private BigDecimal grossRevenue;

  private BigDecimal partnerRevenue;
  private BigDecimal platformFee;
  private BigDecimal revenueSharePercent;

  /** Order statistics for the period */
  private long totalOrders;

  private long completedOrders;
  private long canceledOrders;

  /** Comparison with previous period */
  private BigDecimal previousPeriodRevenue;

  private BigDecimal revenueGrowthPercent;
}
