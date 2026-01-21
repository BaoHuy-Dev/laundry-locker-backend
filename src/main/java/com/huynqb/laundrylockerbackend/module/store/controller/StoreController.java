package com.huynqb.laundrylockerbackend.module.store.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Store operations. */
@Tag(name = TagConstants.ROOT_TAG_STORES)
@RequestMapping(UriParamConstants.ROOT_URI_STORES)
@RestController
@RequiredArgsConstructor
public class StoreController {

  private final StoreService storeService;
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
}
