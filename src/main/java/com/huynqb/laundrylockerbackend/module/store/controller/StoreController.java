package com.huynqb.laundrylockerbackend.module.store.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderRatingResponse;
import com.huynqb.laundrylockerbackend.module.order.service.OrderRatingService;
import com.huynqb.laundrylockerbackend.module.store.dto.request.NearbyStoreRequest;
import com.huynqb.laundrylockerbackend.module.store.dto.response.NearbyStoreResponse;
import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Store operations. */
@Tag(name = TagConstants.ROOT_TAG_STORES)
@RequestMapping(UriParamConstants.ROOT_URI_STORES)
@RestController
@RequiredArgsConstructor
public class StoreController {

  private final StoreService storeService;
  private final OrderRatingService orderRatingService;
  private final ResponseHelper responseHelper;

  /** Get all active stores. */
  @Operation(summary = "Get All Stores", description = "Retrieve all active stores")
  @GetMapping
  public ResponseEntity<ApiResponse<List<StoreResponse>>> getAllStores() {
    List<StoreResponse> stores = storeService.getAllStores();
    return ResponseEntity.ok(responseHelper.success(stores, "STORES_RETRIEVED"));
  }

  /** Get store by ID. */
  @Operation(summary = "Get Store By ID", description = "Retrieve store details by ID")
  @GetMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long id) {
    StoreResponse store = storeService.getStoreById(id);
    return ResponseEntity.ok(responseHelper.success(store, "STORE_RETRIEVED"));
  }

  /** Search nearby stores. */
  @Operation(summary = "Search Nearby Stores", description = "Find stores near a location")
  @GetMapping(UriParamConstants.STORE_NEARBY)
  public ResponseEntity<ApiResponse<List<NearbyStoreResponse>>> getNearbyStores(
      @RequestParam Double latitude,
      @RequestParam Double longitude,
      @RequestParam(defaultValue = "5000") Integer radiusMeters,
      @RequestParam(defaultValue = "20") Integer limit) {
    NearbyStoreRequest request =
        NearbyStoreRequest.builder()
            .latitude(latitude)
            .longitude(longitude)
            .radiusMeters(radiusMeters)
            .limit(limit)
            .build();
    List<NearbyStoreResponse> stores = storeService.findNearbyStores(request);
    return ResponseEntity.ok(responseHelper.success(stores, "NEARBY_STORES_RETRIEVED"));
  }

  /** Get store ratings. */
  @Operation(summary = "Get Store Ratings", description = "Get ratings for a store")
  @GetMapping(UriParamConstants.STORE_RATINGS)
  public ResponseEntity<ApiResponse<Page<OrderRatingResponse>>> getStoreRatings(
      @PathVariable Long storeId, Pageable pageable) {
    Page<OrderRatingResponse> ratings = orderRatingService.getStoreRatings(storeId, pageable);
    return ResponseEntity.ok(responseHelper.success(ratings, "STORE_RATINGS_RETRIEVED"));
  }
}
