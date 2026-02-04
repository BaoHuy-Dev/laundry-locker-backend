package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.DashboardOverviewResponse;
import com.huynqb.laundrylockerbackend.module.admin.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = TagConstants.ROOT_TAG_ADMIN_DASHBOARD, description = "Admin Dashboard APIs")
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_DASHBOARD)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

  private final DashboardService dashboardService;
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "Get Dashboard Overview",
      description = "Get key metrics for admin dashboard")
  @GetMapping(UriParamConstants.ADMIN_OVERVIEW)
  public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getOverview() {
    return ResponseEntity.ok(
        responseHelper.success(dashboardService.getOverview(), "DASHBOARD_RETRIEVED"));
  }
}
