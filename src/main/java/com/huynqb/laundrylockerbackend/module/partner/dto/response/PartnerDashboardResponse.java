package com.huynqb.laundrylockerbackend.module.partner.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dashboard response for partner. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDashboardResponse {

  private Long partnerId;
  private String businessName;

  // Store statistics
  private int totalStores;
  private int activeStores;

  // Staff statistics
  private int totalStaff;

  // Order statistics
  private long totalOrders;
  private long pendingOrders;
  private long completedOrders;
  private long canceledOrders;

  // Revenue statistics
  private BigDecimal totalRevenue;
  private BigDecimal partnerRevenue;
  private BigDecimal platformFee;
  private BigDecimal todayRevenue;
  private BigDecimal monthRevenue;
}
