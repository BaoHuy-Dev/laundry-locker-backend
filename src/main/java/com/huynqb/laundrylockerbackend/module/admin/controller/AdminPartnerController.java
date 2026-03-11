package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerResponse;
import com.huynqb.laundrylockerbackend.module.partner.enums.PartnerStatus;
import com.huynqb.laundrylockerbackend.module.partner.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Admin controller for managing partners. */
@Tag(name = TagConstants.ROOT_TAG_ADMIN_PARTNERS, description = "Admin Partner Management APIs")
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_PARTNERS)
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPartnerController {

  private final PartnerService partnerService;
  private final JwtTokenProvider jwtTokenProvider;
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "Get All Partners",
      description = "Get all partners with optional status filter")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<PartnerResponse>>> getAllPartners(
      @RequestParam(required = false) PartnerStatus status, Pageable pageable) {
    Page<PartnerResponse> partners = partnerService.getAllPartners(status, pageable);
    return ResponseEntity.ok(responseHelper.success(partners, "PARTNERS_RETRIEVED"));
  }

  @Operation(summary = "Get Partner by ID", description = "Get partner details by ID")
  @GetMapping(UriParamConstants.ADMIN_PARTNERS_BY_ID)
  public ResponseEntity<ApiResponse<PartnerResponse>> getPartnerById(@PathVariable Long partnerId) {
    PartnerResponse partner = partnerService.getPartnerById(partnerId);
    return ResponseEntity.ok(responseHelper.success(partner, "PARTNER_RETRIEVED"));
  }

  @Operation(summary = "Approve Partner", description = "Approve a pending partner application")
  @PostMapping(UriParamConstants.ADMIN_PARTNERS_APPROVE)
  public ResponseEntity<ApiResponse<PartnerResponse>> approvePartner(
      @PathVariable Long partnerId, @RequestHeader("Authorization") String authHeader) {
    Long adminUserId = jwtTokenProvider.getUserIdFromToken(authHeader);
    PartnerResponse partner = partnerService.approvePartner(partnerId, adminUserId);
    return ResponseEntity.ok(responseHelper.success(partner, "PARTNER_APPROVED"));
  }

  @Operation(summary = "Reject Partner", description = "Reject a pending partner application")
  @PostMapping(UriParamConstants.ADMIN_PARTNERS_REJECT)
  public ResponseEntity<ApiResponse<PartnerResponse>> rejectPartner(
      @PathVariable Long partnerId, @RequestParam String reason) {
    PartnerResponse partner = partnerService.rejectPartner(partnerId, reason);
    return ResponseEntity.ok(responseHelper.success(partner, "PARTNER_REJECTED"));
  }

  @Operation(summary = "Suspend Partner", description = "Suspend an active partner")
  @PostMapping(UriParamConstants.ADMIN_PARTNERS_SUSPEND)
  public ResponseEntity<ApiResponse<PartnerResponse>> suspendPartner(@PathVariable Long partnerId) {
    PartnerResponse partner = partnerService.suspendPartner(partnerId);
    return ResponseEntity.ok(responseHelper.success(partner, "PARTNER_SUSPENDED"));
  }
}
