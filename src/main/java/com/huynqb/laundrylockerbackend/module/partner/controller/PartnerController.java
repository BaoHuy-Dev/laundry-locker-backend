package com.huynqb.laundrylockerbackend.module.partner.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.BoxResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerResponse;
import com.huynqb.laundrylockerbackend.module.order.dto.request.UpdateOrderWeightRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.GenerateAccessCodeRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.PartnerRegistrationRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.PartnerUpdateRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerDashboardResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerOrderStatisticsResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerRevenueResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.StaffAccessCodeResponse;
import com.huynqb.laundrylockerbackend.module.partner.service.PartnerService;
import com.huynqb.laundrylockerbackend.module.partner.service.StaffAccessCodeService;
import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for partner operations. */
@Tag(name = TagConstants.ROOT_TAG_PARTNER, description = "Partner APIs")
@RequestMapping(UriParamConstants.ROOT_URI_PARTNER)
@RestController
@RequiredArgsConstructor
public class PartnerController {

  private final PartnerService partnerService;
  private final StaffAccessCodeService accessCodeService;
  private final JwtTokenProvider jwtTokenProvider;
  private final ResponseHelper responseHelper;

  // ===== Registration =====

  @Operation(
      summary = "Register as Partner",
      description = "Register current user as a business partner")
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PartnerResponse>> registerAsPartner(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody PartnerRegistrationRequest request) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    PartnerResponse response = partnerService.registerPartner(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(response, "PARTNER_REGISTERED"));
  }

  @Operation(summary = "Get My Partner Profile", description = "Get current user's partner profile")
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PartnerResponse>> getMyPartnerProfile(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    PartnerResponse response = partnerService.getPartnerByUserId(userId);
    return ResponseEntity.ok(responseHelper.success(response, "PARTNER_RETRIEVED"));
  }

  // ===== Dashboard =====

  @Operation(
      summary = "Get Partner Dashboard",
      description = "Get partner dashboard with statistics")
  @GetMapping(UriParamConstants.PARTNER_DASHBOARD)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<PartnerDashboardResponse>> getPartnerDashboard(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    PartnerDashboardResponse dashboard = partnerService.getPartnerDashboard(userId);
    return ResponseEntity.ok(responseHelper.success(dashboard, "DASHBOARD_RETRIEVED"));
  }

  // ===== Order Management =====

  @Operation(
      summary = "Get Pending Orders",
      description = "Get all orders pending for partner's stores (WAITING status)")
  @GetMapping(UriParamConstants.PARTNER_ORDERS_PENDING)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getPendingOrders(
      @RequestHeader("Authorization") String authHeader, Pageable pageable) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    Page<OrderResponse> orders = partnerService.getPendingOrders(userId, pageable);
    return ResponseEntity.ok(responseHelper.success(orders, "ORDERS_RETRIEVED"));
  }

  @Operation(summary = "Get Partner Orders", description = "Get all orders for partner's stores")
  @GetMapping(UriParamConstants.PARTNER_ORDERS)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getPartnerOrders(
      @RequestHeader("Authorization") String authHeader,
      @RequestParam(required = false) String status,
      Pageable pageable) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    Page<OrderResponse> orders = partnerService.getPartnerOrders(userId, status, pageable);
    return ResponseEntity.ok(responseHelper.success(orders, "ORDERS_RETRIEVED"));
  }

  @Operation(
      summary = "Accept Order",
      description = "Accept an order and generate staff access code for collection")
  @PostMapping(UriParamConstants.PARTNER_ORDERS_ACCEPT)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<StaffAccessCodeResponse>> acceptOrder(
      @RequestHeader("Authorization") String authHeader,
      @PathVariable Long orderId,
      @RequestParam(required = false, defaultValue = "24") Integer expirationHours,
      @RequestParam(required = false) String notes) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    StaffAccessCodeResponse response =
        partnerService.acceptOrderAndGenerateCode(userId, orderId, expirationHours, notes);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(response, "ORDER_ACCEPTED"));
  }

  @Operation(summary = "Update Order to Processing", description = "Mark order as being processed")
  @PostMapping(UriParamConstants.PARTNER_ORDERS_PROCESS)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<OrderResponse>> processOrder(
      @RequestHeader("Authorization") String authHeader, @PathVariable Long orderId) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    OrderResponse response = partnerService.updateOrderToProcessing(userId, orderId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_PROCESSING"));
  }

  @Operation(
      summary = "Mark Order Ready",
      description = "Mark order as ready and generate staff access code for return")
  @PostMapping(UriParamConstants.PARTNER_ORDERS_READY)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<StaffAccessCodeResponse>> markOrderReady(
      @RequestHeader("Authorization") String authHeader,
      @PathVariable Long orderId,
      @RequestParam(required = false, defaultValue = "24") Integer expirationHours,
      @RequestParam(required = false) String notes) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    StaffAccessCodeResponse response =
        partnerService.markOrderReadyAndGenerateCode(userId, orderId, expirationHours, notes);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_READY"));
  }

  // ===== Staff Access Code Management =====

  @Operation(
      summary = "Generate Access Code",
      description = "Generate a staff access code for an order")
  @PostMapping(UriParamConstants.PARTNER_ACCESS_CODES_GENERATE)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<StaffAccessCodeResponse>> generateAccessCode(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody GenerateAccessCodeRequest request) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    Long partnerId = partnerService.getPartnerIdByUserId(userId);
    StaffAccessCodeResponse response = accessCodeService.generateAccessCode(partnerId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(response, "CODE_GENERATED"));
  }

  @Operation(
      summary = "Get Access Codes",
      description = "Get all access codes generated by partner")
  @GetMapping(UriParamConstants.PARTNER_ACCESS_CODES)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<Page<StaffAccessCodeResponse>>> getAccessCodes(
      @RequestHeader("Authorization") String authHeader, Pageable pageable) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    Long partnerId = partnerService.getPartnerIdByUserId(userId);
    Page<StaffAccessCodeResponse> codes = accessCodeService.getCodesByPartner(partnerId, pageable);
    return ResponseEntity.ok(responseHelper.success(codes, "CODES_RETRIEVED"));
  }

  @Operation(
      summary = "Get Access Codes by Order",
      description = "Get all access codes for a specific order")
  @GetMapping(UriParamConstants.PARTNER_ACCESS_CODES_BY_ORDER)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<List<StaffAccessCodeResponse>>> getAccessCodesByOrder(
      @PathVariable Long orderId) {
    List<StaffAccessCodeResponse> codes = accessCodeService.getCodesByOrderId(orderId);
    return ResponseEntity.ok(responseHelper.success(codes, "CODES_RETRIEVED"));
  }

  @Operation(summary = "Cancel Access Code", description = "Cancel an active access code")
  @PostMapping(UriParamConstants.PARTNER_ACCESS_CODES_CANCEL)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<Void>> cancelAccessCode(
      @RequestHeader("Authorization") String authHeader, @PathVariable Long codeId) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    Long partnerId = partnerService.getPartnerIdByUserId(userId);
    accessCodeService.cancelAccessCode(codeId, partnerId);
    return ResponseEntity.ok(responseHelper.success(null, "CODE_CANCELLED"));
  }

  // ===== Stores =====

  @Operation(summary = "Get Partner Stores", description = "Get all stores managed by the partner")
  @GetMapping(UriParamConstants.PARTNER_STORES)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<List<StoreResponse>>> getPartnerStores(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    List<StoreResponse> stores = partnerService.getPartnerStores(userId);
    return ResponseEntity.ok(responseHelper.success(stores, "STORES_RETRIEVED"));
  }

  // ===== Order Detail & Weight Update =====

  @Operation(
      summary = "Get Order Detail",
      description = "Get detailed information of a specific order")
  @GetMapping(UriParamConstants.PARTNER_ORDERS_BY_ID)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
      @RequestHeader("Authorization") String authHeader, @PathVariable Long orderId) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    OrderResponse response = partnerService.getOrderDetail(userId, orderId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_RETRIEVED"));
  }

  @Operation(
      summary = "Update Order Weight",
      description = "Update actual weight of an order after collection")
  @PutMapping(UriParamConstants.PARTNER_ORDERS_WEIGHT)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<OrderResponse>> updateOrderWeight(
      @RequestHeader("Authorization") String authHeader,
      @PathVariable Long orderId,
      @Valid @RequestBody UpdateOrderWeightRequest request) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    OrderResponse response = partnerService.updateOrderWeight(userId, orderId, request);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_WEIGHT_UPDATED"));
  }

  // ===== Partner Profile Update =====

  @Operation(
      summary = "Update Partner Profile",
      description = "Update partner business profile information")
  @PutMapping
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<PartnerResponse>> updatePartnerProfile(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody PartnerUpdateRequest request) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    PartnerResponse response = partnerService.updatePartner(userId, request);
    return ResponseEntity.ok(responseHelper.success(response, "PARTNER_UPDATED"));
  }

  // ===== Staff Management =====

  @Operation(summary = "Get Partner Staff", description = "Get all staff members for this partner")
  @GetMapping(UriParamConstants.PARTNER_STAFF)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<List<UserResponse>>> getPartnerStaff(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    List<UserResponse> staff = partnerService.getPartnerStaff(userId);
    return ResponseEntity.ok(responseHelper.success(staff, "STAFF_RETRIEVED"));
  }

  @Operation(summary = "Add Staff to Partner", description = "Add a user as staff to this partner")
  @PostMapping(UriParamConstants.PARTNER_STAFF_BY_ID)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<UserResponse>> addStaffToPartner(
      @RequestHeader("Authorization") String authHeader, @PathVariable Long staffId) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    UserResponse response = partnerService.addStaffToPartner(userId, staffId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(response, "STAFF_ADDED"));
  }

  @Operation(
      summary = "Remove Staff from Partner",
      description = "Remove a staff member from this partner")
  @DeleteMapping(UriParamConstants.PARTNER_STAFF_BY_ID)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<Void>> removeStaffFromPartner(
      @RequestHeader("Authorization") String authHeader, @PathVariable Long staffId) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    partnerService.removeStaffFromPartner(userId, staffId);
    return ResponseEntity.ok(responseHelper.success(null, "STAFF_REMOVED"));
  }

  // ===== Partner Lockers =====

  @Operation(
      summary = "Get Partner Lockers",
      description = "Get all lockers from stores managed by the partner")
  @GetMapping(UriParamConstants.PARTNER_LOCKERS)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<List<LockerResponse>>> getPartnerLockers(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    List<LockerResponse> lockers = partnerService.getPartnerLockers(userId);
    return ResponseEntity.ok(responseHelper.success(lockers, "LOCKERS_RETRIEVED"));
  }

  @Operation(
      summary = "Get Available Boxes by Locker",
      description = "Get available boxes in a specific locker")
  @GetMapping(UriParamConstants.PARTNER_LOCKERS_BOXES_AVAILABLE)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<List<BoxResponse>>> getAvailableBoxesByLocker(
      @RequestHeader("Authorization") String authHeader, @PathVariable Long lockerId) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    List<BoxResponse> boxes = partnerService.getPartnerLockerAvailableBoxes(userId, lockerId);
    return ResponseEntity.ok(responseHelper.success(boxes, "BOXES_RETRIEVED"));
  }

  // ===== Partner Revenue & Statistics =====

  @Operation(
      summary = "Get Partner Revenue",
      description = "Get revenue report for a specific time period")
  @GetMapping(UriParamConstants.PARTNER_REVENUE)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<PartnerRevenueResponse>> getPartnerRevenue(
      @RequestHeader("Authorization") String authHeader,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    PartnerRevenueResponse revenue = partnerService.getPartnerRevenue(userId, fromDate, toDate);
    return ResponseEntity.ok(responseHelper.success(revenue, "REVENUE_RETRIEVED"));
  }

  @Operation(
      summary = "Get Order Statistics",
      description = "Get comprehensive order statistics for the partner")
  @GetMapping(UriParamConstants.PARTNER_ORDERS_STATISTICS)
  @PreAuthorize("hasRole('PARTNER')")
  public ResponseEntity<ApiResponse<PartnerOrderStatisticsResponse>> getOrderStatistics(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    PartnerOrderStatisticsResponse stats = partnerService.getPartnerOrderStatistics(userId);
    return ResponseEntity.ok(responseHelper.success(stats, "STATISTICS_RETRIEVED"));
  }
}
