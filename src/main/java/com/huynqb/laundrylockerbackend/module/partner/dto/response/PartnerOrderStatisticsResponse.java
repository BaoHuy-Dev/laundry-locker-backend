package com.huynqb.laundrylockerbackend.module.partner.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for partner order statistics. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerOrderStatisticsResponse {

  private Long partnerId;

  /** Overall statistics */
  private long totalOrders;

  private long todayOrders;
  private long weekOrders;
  private long monthOrders;

  /** Status breakdown */
  private long initializedOrders;

  private long waitingOrders;
  private long collectedOrders;
  private long processingOrders;
  private long readyOrders;
  private long returnedOrders;
  private long completedOrders;
  private long canceledOrders;

  /** Revenue metrics */
  private BigDecimal totalRevenue;

  private BigDecimal todayRevenue;
  private BigDecimal weekRevenue;
  private BigDecimal monthRevenue;
  private BigDecimal averageOrderValue;

  /** Orders by store (storeId -> count) */
  private Map<Long, Long> ordersByStore;
}
