package com.huynqb.laundrylockerbackend.module.loyalty.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.request.RedeemPointsRequest;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.request.RedeemStampRequest;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.ExpiringPointsResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.LoyaltyAccountResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.LoyaltySummaryResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.PointTransactionResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.RewardsResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.StampCardResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.StampTransactionResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user loyalty operations. Points system: 10,000 VND = 1 point, 10,000 points =
 * 10,000 VND discount Stamp system: Use 6 times = get 1 free
 */
@Tag(name = TagConstants.ROOT_TAG_LOYALTY, description = "Loyalty Points & Stamps APIs")
@RequestMapping(UriParamConstants.ROOT_URI_LOYALTY)
@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LoyaltyController {

  private final LoyaltyService loyaltyService;
  private final JwtTokenProvider jwtTokenProvider;
  private final ResponseHelper responseHelper;

  // ===== Summary =====

  @Operation(
      summary = "Get Loyalty Summary",
      description = "Get complete loyalty summary including points and all stamp cards")
  @GetMapping(UriParamConstants.LOYALTY_SUMMARY)
  public ResponseEntity<ApiResponse<LoyaltySummaryResponse>> getLoyaltySummary(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    LoyaltySummaryResponse summary = loyaltyService.getLoyaltySummary(userId);
    return ResponseEntity.ok(responseHelper.success(summary, "LOYALTY_SUMMARY_RETRIEVED"));
  }

  // ===== Points =====

  @Operation(
      summary = "Get Points Account",
      description = "Get current points balance and statistics")
  @GetMapping(UriParamConstants.LOYALTY_POINTS)
  public ResponseEntity<ApiResponse<LoyaltyAccountResponse>> getPointsAccount(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    LoyaltyAccountResponse account = loyaltyService.getAccountResponse(userId);
    return ResponseEntity.ok(responseHelper.success(account, "POINTS_ACCOUNT_RETRIEVED"));
  }

  @Operation(summary = "Get Points History", description = "Get point transaction history")
  @GetMapping(UriParamConstants.LOYALTY_POINTS_HISTORY)
  public ResponseEntity<ApiResponse<Page<PointTransactionResponse>>> getPointsHistory(
      @RequestHeader("Authorization") String authHeader, Pageable pageable) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    Page<PointTransactionResponse> history =
        loyaltyService.getPointTransactionHistory(userId, pageable);
    return ResponseEntity.ok(responseHelper.success(history, "POINTS_HISTORY_RETRIEVED"));
  }

  @Operation(
      summary = "Redeem Points",
      description = "Redeem points for discount on an order. 1 point = 1 VND")
  @PostMapping(UriParamConstants.LOYALTY_REDEEM_POINTS)
  public ResponseEntity<ApiResponse<PointTransactionResponse>> redeemPoints(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody RedeemPointsRequest request) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    PointTransactionResponse result = loyaltyService.redeemPoints(userId, request);
    return ResponseEntity.ok(responseHelper.success(result, "POINTS_REDEEMED"));
  }

  // ===== Stamps =====

  @Operation(summary = "Get All Stamp Cards", description = "Get all stamp cards for the user")
  @GetMapping(UriParamConstants.LOYALTY_STAMPS)
  public ResponseEntity<ApiResponse<List<StampCardResponse>>> getStampCards(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    List<StampCardResponse> stampCards = loyaltyService.getUserStampCards(userId);
    return ResponseEntity.ok(responseHelper.success(stampCards, "STAMP_CARDS_RETRIEVED"));
  }

  @Operation(
      summary = "Get Stamp Card Details",
      description = "Get specific stamp card with transaction history")
  @GetMapping(UriParamConstants.LOYALTY_STAMPS_BY_ID)
  public ResponseEntity<ApiResponse<StampCardResponse>> getStampCard(
      @RequestHeader("Authorization") String authHeader, @PathVariable Long stampCardId) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    StampCardResponse stampCard = loyaltyService.getStampCardResponse(userId, stampCardId);
    return ResponseEntity.ok(responseHelper.success(stampCard, "STAMP_CARD_RETRIEVED"));
  }

  @Operation(
      summary = "Redeem Stamp Reward",
      description = "Redeem a free reward from stamp card for an order")
  @PostMapping(UriParamConstants.LOYALTY_REDEEM_STAMP)
  public ResponseEntity<ApiResponse<StampTransactionResponse>> redeemStampReward(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody RedeemStampRequest request) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    StampTransactionResponse result = loyaltyService.redeemStampReward(userId, request);
    return ResponseEntity.ok(responseHelper.success(result, "STAMP_REWARD_REDEEMED"));
  }

  // ===== Rewards =====

  @Operation(
      summary = "Get Available Rewards",
      description = "Get list of rewards that can be redeemed with points")
  @GetMapping(UriParamConstants.LOYALTY_REWARDS)
  public ResponseEntity<ApiResponse<RewardsResponse>> getAvailableRewards(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    RewardsResponse rewards = loyaltyService.getAvailableRewards(userId);
    return ResponseEntity.ok(responseHelper.success(rewards, "REWARDS_RETRIEVED"));
  }

  @Operation(
      summary = "Redeem Reward",
      description = "Redeem a loyalty reward and receive a voucher code for order creation")
  @PostMapping(UriParamConstants.LOYALTY_REDEEM_REWARD)
  public ResponseEntity<ApiResponse<RewardsResponse.RedeemedReward>> redeemReward(
      @RequestHeader("Authorization") String authHeader, @PathVariable Long rewardId) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    RewardsResponse.RedeemedReward redeemedReward = loyaltyService.redeemReward(userId, rewardId);
    return ResponseEntity.ok(responseHelper.success(redeemedReward, "REWARD_REDEEMED"));
  }

  @Operation(
      summary = "Get Expiring Points",
      description = "Get points that will expire soon and recommendations")
  @GetMapping(UriParamConstants.LOYALTY_EXPIRING_POINTS)
  public ResponseEntity<ApiResponse<ExpiringPointsResponse>> getExpiringPoints(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = jwtTokenProvider.getUserIdFromToken(authHeader);
    ExpiringPointsResponse expiringPoints = loyaltyService.getExpiringPoints(userId);
    return ResponseEntity.ok(responseHelper.success(expiringPoints, "EXPIRING_POINTS_RETRIEVED"));
  }
}
