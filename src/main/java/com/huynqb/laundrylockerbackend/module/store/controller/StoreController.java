package com.huynqb.laundrylockerbackend.module.store.controller;

import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.store.service.StoreService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Store operations. */
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

  private final StoreService storeService;

  /** Get all active stores. */
  @GetMapping
  public ResponseEntity<ApiResponse<List<StoreResponse>>> getAllStores() {
    List<StoreResponse> stores = storeService.getAllStores();
    return ResponseEntity.ok(ApiResponse.success(stores, "STORES_RETRIEVED"));
  }

  /** Get store by ID. */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long id) {
    StoreResponse store = storeService.getStoreById(id);
    return ResponseEntity.ok(ApiResponse.success(store, "STORE_RETRIEVED"));
  }
}
