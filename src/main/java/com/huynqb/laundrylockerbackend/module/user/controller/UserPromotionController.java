package com.huynqb.laundrylockerbackend.module.user.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.PromotionResponse;
import com.huynqb.laundrylockerbackend.module.admin.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for promotions. Any authenticated user can view active promotions and
 * validate codes.
 */
@Tag(
    name = TagConstants.ROOT_TAG_PROMOTIONS,
    description = "Public Promotion APIs for mobile users")
@RequestMapping(UriParamConstants.ROOT_URI_PROMOTIONS)
@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserPromotionController {

  private final PromotionService promotionService;
  private final ResponseHelper responseHelper;

  /** Get all currently active promotions. */
  @Operation(
      summary = "Get Active Promotions",
      description = "Get all currently active promotions available for users")
  @GetMapping(UriParamConstants.PROMOTION_ACTIVE)
  public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
    List<PromotionResponse> promotions = promotionService.getActivePromotions();
    return ResponseEntity.ok(responseHelper.success(promotions, "ACTIVE_PROMOTIONS_RETRIEVED"));
  }

  /** Validate a promotion code. */
  @Operation(
      summary = "Validate Promotion Code",
      description = "Check if a promotion code is valid and currently active")
  @GetMapping(UriParamConstants.PROMOTION_VALIDATE_CODE)
  public ResponseEntity<ApiResponse<PromotionResponse>> validatePromotionCode(
      @PathVariable String code) {
    PromotionResponse response = promotionService.validatePromotionCode(code);
    return ResponseEntity.ok(responseHelper.success(response, "PROMOTION_VALID"));
  }
}
