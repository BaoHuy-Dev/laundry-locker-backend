package com.huynqb.laundrylockerbackend.module.partner.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.GenerateAccessCodeRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.PartnerRegistrationRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerDashboardResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.StaffAccessCodeResponse;
import com.huynqb.laundrylockerbackend.module.partner.service.PartnerService;
import com.huynqb.laundrylockerbackend.module.partner.service.StaffAccessCodeService;
import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  @PreAuthorize("hasRole('PARTNER')")
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
}
