package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.PromotionRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.PromotionResponse;
import com.huynqb.laundrylockerbackend.module.admin.service.PromotionService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for managing promotions (Admin only). */
@Tag(name = "Admin - Promotions", description = "Promotion management APIs")
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_PROMOTIONS)
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromotionController {

  private final PromotionService promotionService;
  private final JwtTokenProvider jwtTokenProvider;
  private final ResponseHelper responseHelper;

  /** Create a new promotion. */
  @Operation(summary = "Create Promotion", description = "Create a new promotion/discount code")
  @PostMapping
  public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
      @Valid @RequestBody PromotionRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long adminId = extractUserId(authHeader);
    PromotionResponse response = promotionService.createPromotion(request, adminId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(response, "PROMOTION_CREATED"));
  }

  /** Get all promotions with pagination. */
  @Operation(summary = "Get All Promotions", description = "Get all promotions with pagination")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<PromotionResponse>>> getPromotions(Pageable pageable) {
    Page<PromotionResponse> promotions = promotionService.getPromotions(pageable);
    return ResponseEntity.ok(responseHelper.success(promotions, "PROMOTIONS_RETRIEVED"));
  }

  /** Get promotion by ID. */
  @Operation(summary = "Get Promotion", description = "Get promotion by ID")
  @GetMapping(UriParamConstants.ADMIN_PROMOTION_BY_ID)
  public ResponseEntity<ApiResponse<PromotionResponse>> getPromotion(
      @PathVariable Long promotionId) {
    PromotionResponse response = promotionService.getPromotion(promotionId);
    return ResponseEntity.ok(responseHelper.success(response, "PROMOTION_RETRIEVED"));
  }

  /** Get promotions by status. */
  @Operation(
      summary = "Get Promotions by Status",
      description = "Get promotions filtered by status")
  @GetMapping("/status/{status}")
  public ResponseEntity<ApiResponse<Page<PromotionResponse>>> getPromotionsByStatus(
      @PathVariable String status, Pageable pageable) {
    Page<PromotionResponse> promotions = promotionService.getPromotionsByStatus(status, pageable);
    return ResponseEntity.ok(responseHelper.success(promotions, "PROMOTIONS_RETRIEVED"));
  }

  /** Get active promotions. */
  @Operation(summary = "Get Active Promotions", description = "Get all currently active promotions")
  @GetMapping("/active")
  public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
    List<PromotionResponse> promotions = promotionService.getActivePromotions();
    return ResponseEntity.ok(responseHelper.success(promotions, "ACTIVE_PROMOTIONS_RETRIEVED"));
  }

  /** Update a promotion. */
  @Operation(summary = "Update Promotion", description = "Update an existing promotion")
  @PutMapping(UriParamConstants.ADMIN_PROMOTION_BY_ID)
  public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
      @PathVariable Long promotionId, @Valid @RequestBody PromotionRequest request) {
    PromotionResponse response = promotionService.updatePromotion(promotionId, request);
    return ResponseEntity.ok(responseHelper.success(response, "PROMOTION_UPDATED"));
  }

  /** Validate a promotion code. */
  @Operation(
      summary = "Validate Promotion Code",
      description = "Validate if a promotion code is valid and active")
  @GetMapping(UriParamConstants.ADMIN_PROMOTION_VALIDATE)
  @PreAuthorize("isAuthenticated()") // Allow all authenticated users to validate
  public ResponseEntity<ApiResponse<PromotionResponse>> validatePromotionCode(
      @PathVariable String code) {
    PromotionResponse response = promotionService.validatePromotionCode(code);
    return ResponseEntity.ok(responseHelper.success(response, "PROMOTION_VALID"));
  }

  /** Search promotions. */
  @Operation(summary = "Search Promotions", description = "Search promotions by keyword")
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<Page<PromotionResponse>>> searchPromotions(
      @RequestParam String keyword, Pageable pageable) {
    Page<PromotionResponse> promotions = promotionService.searchPromotions(keyword, pageable);
    return ResponseEntity.ok(responseHelper.success(promotions, "PROMOTIONS_RETRIEVED"));
  }

  /** Delete a promotion. */
  @Operation(summary = "Delete Promotion", description = "Delete or deactivate a promotion")
  @DeleteMapping(UriParamConstants.ADMIN_PROMOTION_BY_ID)
  public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long promotionId) {
    promotionService.deletePromotion(promotionId);
    return ResponseEntity.ok(responseHelper.success(null, "PROMOTION_DELETED"));
  }

  private Long extractUserId(String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return jwtTokenProvider.getUserIdFromToken(token);
  }
}
