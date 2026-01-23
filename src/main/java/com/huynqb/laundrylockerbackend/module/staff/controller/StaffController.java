package com.huynqb.laundrylockerbackend.module.staff.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerResponse;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.staff.dto.request.StaffUnlockBoxRequest;
import com.huynqb.laundrylockerbackend.module.staff.dto.response.StaffOrderSummaryResponse;
import com.huynqb.laundrylockerbackend.module.staff.dto.response.StaffUnlockBoxResponse;
import com.huynqb.laundrylockerbackend.module.staff.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for staff-specific operations. */
@Tag(name = TagConstants.ROOT_TAG_STAFF, description = "Staff Operations APIs")
@RequestMapping(UriParamConstants.ROOT_URI_STAFF)
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class StaffController {

  private final StaffService staffService;
  private final JwtTokenProvider jwtTokenProvider;
  private final ResponseHelper responseHelper;

  /** Get order summary for staff dashboard. */
  @Operation(
      summary = "Get Order Summary",
      description = "Get summary of orders by status for staff dashboard")
  @GetMapping(UriParamConstants.STAFF_ORDERS)
  public ResponseEntity<ApiResponse<StaffOrderSummaryResponse>> getOrderSummary() {
    StaffOrderSummaryResponse summary = staffService.getOrderSummary();
    return ResponseEntity.ok(responseHelper.success(summary, "SUMMARY_RETRIEVED"));
  }

  /** Get waiting orders (ready for collection). */
  @Operation(
      summary = "Get Waiting Orders",
      description = "Get orders waiting for staff collection")
  @GetMapping(UriParamConstants.STAFF_ORDERS_WAITING)
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getWaitingOrders(Pageable pageable) {
    Page<OrderResponse> orders = staffService.getWaitingOrders(pageable);
    return ResponseEntity.ok(responseHelper.success(orders, "ORDERS_RETRIEVED"));
  }

  /** Get processing orders (collected or being processed). */
  @Operation(summary = "Get Processing Orders", description = "Get orders being processed")
  @GetMapping(UriParamConstants.STAFF_ORDERS_PROCESSING)
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getProcessingOrders(Pageable pageable) {
    Page<OrderResponse> orders = staffService.getProcessingOrders(pageable);
    return ResponseEntity.ok(responseHelper.success(orders, "ORDERS_RETRIEVED"));
  }

  /** Get ready orders (ready to return to locker). */
  @Operation(
      summary = "Get Ready Orders",
      description = "Get orders ready to be returned to locker")
  @GetMapping(UriParamConstants.STAFF_ORDERS_READY)
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getReadyOrders(Pageable pageable) {
    Page<OrderResponse> orders = staffService.getReadyOrders(pageable);
    return ResponseEntity.ok(responseHelper.success(orders, "ORDERS_RETRIEVED"));
  }

  /** Assign order to self. */
  @Operation(summary = "Assign Order", description = "Staff assigns an order to themselves")
  @PostMapping(UriParamConstants.STAFF_ASSIGN)
  public ResponseEntity<ApiResponse<OrderResponse>> assignOrder(
      @PathVariable Long orderId, @RequestHeader("Authorization") String authHeader) {
    Long staffId = extractUserId(authHeader);
    OrderResponse response = staffService.assignOrderToStaff(orderId, staffId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_ASSIGNED"));
  }

  /** Get orders assigned to current staff. */
  @Operation(
      summary = "Get My Assigned Orders",
      description = "Get orders assigned to current staff member")
  @GetMapping(UriParamConstants.STAFF_MY_ASSIGNED)
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyAssignedOrders(
      Pageable pageable, @RequestHeader("Authorization") String authHeader) {
    Long staffId = extractUserId(authHeader);
    Page<OrderResponse> orders = staffService.getMyAssignedOrders(staffId, pageable);
    return ResponseEntity.ok(responseHelper.success(orders, "ORDERS_RETRIEVED"));
  }

  /** Get all lockers for staff. */
  @Operation(
      summary = "Get Lockers",
      description = "Get all lockers with box availability for staff")
  @GetMapping(UriParamConstants.STAFF_LOCKERS)
  public ResponseEntity<ApiResponse<List<LockerResponse>>> getLockers(
      @RequestParam(required = false) Long storeId) {
    List<LockerResponse> lockers;
    if (storeId != null) {
      lockers = staffService.getLockersByStore(storeId);
    } else {
      lockers = staffService.getAllLockers();
    }
    return ResponseEntity.ok(responseHelper.success(lockers, "LOCKERS_RETRIEVED"));
  }

  /** Unlock box using master PIN. */
  @Operation(
      summary = "Unlock Box",
      description = "Staff unlocks a box using master PIN for collection or return")
  @PostMapping(UriParamConstants.STAFF_UNLOCK_BOX)
  public ResponseEntity<ApiResponse<StaffUnlockBoxResponse>> unlockBox(
      @Valid @RequestBody StaffUnlockBoxRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long staffId = extractUserId(authHeader);
    StaffUnlockBoxResponse response = staffService.unlockBox(request, staffId);
    String code = response.getSuccess() ? "BOX_UNLOCKED" : "UNLOCK_FAILED";
    return ResponseEntity.ok(responseHelper.success(response, code));
  }

  private Long extractUserId(String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return jwtTokenProvider.getUserIdFromToken(token);
  }
}
