package com.huynqb.laundrylockerbackend.module.staff.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for staff performance report. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Staff performance report")
public class StaffPerformanceResponse {

  @Schema(description = "Staff member ID")
  private Long staffId;

  @Schema(description = "Staff member name")
  private String staffName;

  @Schema(description = "Report period start")
  private LocalDate periodStart;

  @Schema(description = "Report period end")
  private LocalDate periodEnd;

  @Schema(description = "Total orders handled")
  private Integer totalOrdersHandled;

  @Schema(description = "Orders completed successfully")
  private Integer ordersCompleted;

  @Schema(description = "Average handling time in minutes")
  private Double avgHandlingTimeMinutes;

  @Schema(description = "Customer ratings received")
  private RatingInfo ratings;

  @Schema(description = "Attendance information")
  private AttendanceInfo attendance;

  @Schema(description = "Issues reported by staff")
  private Integer issuesReported;

  @Schema(description = "Issues resolved by staff")
  private Integer issuesResolved;

  @Schema(description = "Tasks breakdown by type")
  private Map<String, Integer> taskBreakdown;

  @Schema(description = "Performance trend: IMPROVING, STABLE, DECLINING")
  private String trend;

  @Schema(description = "Performance score (0-100)")
  private Double performanceScore;

  @Schema(description = "Daily performance breakdown")
  private List<DailyPerformance> dailyPerformance;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Rating information")
  public static class RatingInfo {
    @Schema(description = "Average rating received")
    private Double averageRating;

    @Schema(description = "Total ratings count")
    private Integer totalRatings;

    @Schema(description = "5-star ratings count")
    private Integer fiveStarCount;

    @Schema(description = "Positive feedback percentage")
    private Double positivePercentage;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Attendance information")
  public static class AttendanceInfo {
    @Schema(description = "Total working days")
    private Integer totalDays;

    @Schema(description = "Days present")
    private Integer daysPresent;

    @Schema(description = "Days absent")
    private Integer daysAbsent;

    @Schema(description = "Days late")
    private Integer daysLate;

    @Schema(description = "Total hours worked")
    private Double totalHoursWorked;

    @Schema(description = "Attendance rate percentage")
    private Double attendanceRate;

    @Schema(description = "Punctuality rate percentage")
    private Double punctualityRate;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Daily performance")
  public static class DailyPerformance {
    @Schema(description = "Date")
    private LocalDate date;

    @Schema(description = "Orders handled")
    private Integer ordersHandled;

    @Schema(description = "Hours worked")
    private Double hoursWorked;

    @Schema(description = "Average rating for the day")
    private Double avgRating;

    @Schema(description = "Issues handled")
    private Integer issuesHandled;
  }
}
