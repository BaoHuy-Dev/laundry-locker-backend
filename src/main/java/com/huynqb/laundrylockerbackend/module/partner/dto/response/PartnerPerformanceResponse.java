package com.huynqb.laundrylockerbackend.module.partner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for partner performance report. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Partner performance report")
public class PartnerPerformanceResponse {

  @Schema(description = "Report period start date")
  private LocalDate periodStart;

  @Schema(description = "Report period end date")
  private LocalDate periodEnd;

  @Schema(description = "Partner ID")
  private Long partnerId;

  @Schema(description = "Partner name")
  private String partnerName;

  @Schema(description = "Order statistics")
  private OrderStats orderStats;

  @Schema(description = "Revenue statistics")
  private RevenueStats revenueStats;

  @Schema(description = "Rating statistics")
  private RatingStats ratingStats;

  @Schema(description = "Service performance")
  private List<ServicePerformance> servicePerformance;

  @Schema(description = "Daily breakdown")
  private List<DailyStats> dailyStats;

  @Schema(description = "Comparison with previous period")
  private PeriodComparison comparison;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Order statistics")
  public static class OrderStats {
    @Schema(description = "Total orders received")
    private Integer totalOrders;

    @Schema(description = "Completed orders")
    private Integer completedOrders;

    @Schema(description = "Cancelled orders")
    private Integer cancelledOrders;

    @Schema(description = "Pending orders")
    private Integer pendingOrders;

    @Schema(description = "Completion rate percentage")
    private Double completionRate;

    @Schema(description = "Average processing time in hours")
    private Double avgProcessingTimeHours;

    @Schema(description = "On-time delivery rate percentage")
    private Double onTimeDeliveryRate;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Revenue statistics")
  public static class RevenueStats {
    @Schema(description = "Gross revenue")
    private BigDecimal grossRevenue;

    @Schema(description = "Net revenue after deductions")
    private BigDecimal netRevenue;

    @Schema(description = "Platform fees paid")
    private BigDecimal platformFees;

    @Schema(description = "Refunds issued")
    private BigDecimal refunds;

    @Schema(description = "Average order value")
    private BigDecimal avgOrderValue;

    @Schema(description = "Revenue by payment method")
    private Map<String, BigDecimal> revenueByPaymentMethod;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Rating statistics")
  public static class RatingStats {
    @Schema(description = "Average overall rating")
    private Double averageRating;

    @Schema(description = "Total number of reviews")
    private Integer totalReviews;

    @Schema(description = "Rating distribution (5-star to 1-star)")
    private Map<Integer, Integer> ratingDistribution;

    @Schema(description = "Average service quality rating")
    private Double avgServiceRating;

    @Schema(description = "Average speed rating")
    private Double avgSpeedRating;

    @Schema(description = "Average staff rating")
    private Double avgStaffRating;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Service performance")
  public static class ServicePerformance {
    @Schema(description = "Service ID")
    private Long serviceId;

    @Schema(description = "Service name")
    private String serviceName;

    @Schema(description = "Order count for this service")
    private Integer orderCount;

    @Schema(description = "Revenue from this service")
    private BigDecimal revenue;

    @Schema(description = "Percentage of total orders")
    private Double percentageOfTotal;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Daily statistics")
  public static class DailyStats {
    @Schema(description = "Date")
    private LocalDate date;

    @Schema(description = "Orders count")
    private Integer orders;

    @Schema(description = "Revenue")
    private BigDecimal revenue;

    @Schema(description = "Average rating for the day")
    private Double avgRating;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Period comparison")
  public static class PeriodComparison {
    @Schema(description = "Order change percentage")
    private Double orderChangePercent;

    @Schema(description = "Revenue change percentage")
    private Double revenueChangePercent;

    @Schema(description = "Rating change")
    private Double ratingChange;

    @Schema(description = "Trend: IMPROVING, STABLE, DECLINING")
    private String trend;
  }
}
