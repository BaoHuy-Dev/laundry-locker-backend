package com.huynqb.laundrylockerbackend.module.staff.dto.response;

import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for staff order summary. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffOrderSummaryResponse {

  private Long waitingCount;
  private Long processingCount;
  private Long readyCount;
  private Long collectedCount;
  private List<OrderResponse> recentOrders;
}
