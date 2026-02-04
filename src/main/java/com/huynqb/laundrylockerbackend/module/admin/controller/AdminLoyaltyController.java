package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.request.AdjustPointsRequest;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.LoyaltySummaryResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.PointTransactionResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Admin controller for managing loyalty program. */
@Tag(name = TagConstants.ROOT_TAG_ADMIN_LOYALTY, description = "Admin Loyalty Management APIs")
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_LOYALTY)
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLoyaltyController {

  private final LoyaltyService loyaltyService;
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "Get User Loyalty Summary",
      description = "Get loyalty summary for a specific user")
  @GetMapping(UriParamConstants.ADMIN_LOYALTY_BY_USER)
  public ResponseEntity<ApiResponse<LoyaltySummaryResponse>> getUserLoyaltySummary(
      @PathVariable Long userId) {
    LoyaltySummaryResponse summary = loyaltyService.getLoyaltySummary(userId);
    return ResponseEntity.ok(responseHelper.success(summary, "LOYALTY_SUMMARY_RETRIEVED"));
  }

  @Operation(
      summary = "Adjust User Points",
      description = "Add or subtract points from user account (use negative for subtract)")
  @PostMapping(UriParamConstants.ADMIN_LOYALTY_ADJUST_POINTS)
  public ResponseEntity<ApiResponse<PointTransactionResponse>> adjustUserPoints(
      @PathVariable Long userId, @Valid @RequestBody AdjustPointsRequest request) {
    request.setUserId(userId);
    PointTransactionResponse result = loyaltyService.adjustPoints(request);
    return ResponseEntity.ok(responseHelper.success(result, "POINTS_ADJUSTED"));
  }

  @Operation(
      summary = "Get User Points History",
      description = "Get point transaction history for a specific user")
  @GetMapping(UriParamConstants.ADMIN_LOYALTY_BY_USER + "/history")
  public ResponseEntity<ApiResponse<Page<PointTransactionResponse>>> getUserPointsHistory(
      @PathVariable Long userId, Pageable pageable) {
    Page<PointTransactionResponse> history =
        loyaltyService.getPointTransactionHistory(userId, pageable);
    return ResponseEntity.ok(responseHelper.success(history, "POINTS_HISTORY_RETRIEVED"));
  }

  @Operation(
      summary = "Get Loyalty Statistics",
      description = "Get overall loyalty program statistics")
  @GetMapping(UriParamConstants.ADMIN_LOYALTY_STATISTICS)
  public ResponseEntity<ApiResponse<Map<String, Object>>> getLoyaltyStatistics() {
    Map<String, Object> stats = loyaltyService.getLoyaltyStatistics();
    return ResponseEntity.ok(responseHelper.success(stats, "LOYALTY_STATISTICS_RETRIEVED"));
  }
}
