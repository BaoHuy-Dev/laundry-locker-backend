package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.OrderStatisticsResponse;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.RevenueReportResponse;
import com.huynqb.laundrylockerbackend.module.admin.service.AdminOrderService;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for admin order management. */
@Tag(name = TagConstants.ROOT_TAG_ADMIN_ORDERS, description = "Admin Order Management APIs")
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_ORDERS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

  private final AdminOrderService adminOrderService;
  private final ResponseHelper responseHelper;

  /** Get all orders with optional status filter. */
  @Operation(
      summary = "Get All Orders",
      description = "Retrieve all orders with optional status filter (Admin only)")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
      @RequestParam(required = false) OrderStatus status, Pageable pageable) {
    Page<OrderResponse> orders = adminOrderService.getAllOrders(status, pageable);
    return ResponseEntity.ok(responseHelper.success(orders, "ORDERS_RETRIEVED"));
  }

  /** Get order by ID. */
  @Operation(summary = "Get Order By ID", description = "Retrieve order details by ID (Admin only)")
  @GetMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
    OrderResponse order = adminOrderService.getOrderById(id);
    return ResponseEntity.ok(responseHelper.success(order, "ORDER_RETRIEVED"));
  }

  /** Force update order status. */
  @Operation(
      summary = "Update Order Status",
      description = "Force update order status (Admin override)")
  @PutMapping(UriParamConstants.ADMIN_STATUS)
  public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
      @PathVariable Long id, @RequestParam OrderStatus status) {
    OrderResponse order = adminOrderService.updateOrderStatus(id, status);
    return ResponseEntity.ok(responseHelper.success(order, "ORDER_STATUS_UPDATED"));
  }

  /** Get order statistics. */
  @Operation(
      summary = "Get Order Statistics",
      description = "Get order statistics and metrics (Admin only)")
  @GetMapping(UriParamConstants.ADMIN_ORDERS_STATISTICS)
  public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getOrderStatistics() {
    OrderStatisticsResponse stats = adminOrderService.getOrderStatistics();
    return ResponseEntity.ok(responseHelper.success(stats, "STATISTICS_RETRIEVED"));
  }

  /** Get revenue report. */
  @Operation(
      summary = "Get Revenue Report",
      description = "Get revenue report with breakdown (Admin only)")
  @GetMapping(UriParamConstants.ADMIN_REVENUE)
  public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport() {
    RevenueReportResponse report = adminOrderService.getRevenueReport();
    return ResponseEntity.ok(responseHelper.success(report, "REVENUE_REPORT_RETRIEVED"));
  }
}
