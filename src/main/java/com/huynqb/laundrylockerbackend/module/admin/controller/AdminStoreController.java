package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateStoreRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.UpdateUserStatusRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminStoreResponse;
import com.huynqb.laundrylockerbackend.module.admin.service.AdminStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = TagConstants.ROOT_TAG_ADMIN_STORES, description = "Admin Store Management APIs")
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_STORES)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStoreController {

  private final AdminStoreService adminStoreService;
  private final ResponseHelper responseHelper;

  @Operation(summary = "Get All Stores", description = "Retrieve all stores with pagination")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminStoreResponse>>> getAllStores(Pageable pageable) {
    return ResponseEntity.ok(
        responseHelper.success(adminStoreService.getAllStores(pageable), "STORES_RETRIEVED"));
  }

  @Operation(summary = "Get Store By ID", description = "Retrieve store details by ID")
  @GetMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<AdminStoreResponse>> getStoreById(@PathVariable Long id) {
    return ResponseEntity.ok(
        responseHelper.success(adminStoreService.getStoreById(id), "STORE_RETRIEVED"));
  }

  @Operation(summary = "Create Store", description = "Create a new store")
  @PostMapping
  public ResponseEntity<ApiResponse<AdminStoreResponse>> createStore(
      @Valid @RequestBody CreateStoreRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(adminStoreService.createStore(request), "STORE_CREATED"));
  }

  @Operation(summary = "Update Store", description = "Update store information")
  @PutMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<AdminStoreResponse>> updateStore(
      @PathVariable Long id, @Valid @RequestBody CreateStoreRequest request) {
    return ResponseEntity.ok(
        responseHelper.success(adminStoreService.updateStore(id, request), "STORE_UPDATED"));
  }

  @Operation(summary = "Update Store Status", description = "Activate or deactivate a store")
  @PutMapping(UriParamConstants.ADMIN_STATUS)
  public ResponseEntity<ApiResponse<AdminStoreResponse>> updateStoreStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
    return ResponseEntity.ok(
        responseHelper.success(
            adminStoreService.updateStoreStatus(id, request.getEnabled()), "STORE_STATUS_UPDATED"));
  }

  @Operation(summary = "Delete Store", description = "Soft delete a store")
  @DeleteMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long id) {
    adminStoreService.deleteStore(id);
    return ResponseEntity.ok(responseHelper.success("STORE_DELETED"));
  }
}
