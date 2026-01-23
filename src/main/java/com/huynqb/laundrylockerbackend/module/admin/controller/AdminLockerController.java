package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateBoxRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateLockerRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminLockerResponse;
import com.huynqb.laundrylockerbackend.module.admin.service.AdminLockerService;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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

@Tag(name = TagConstants.ROOT_TAG_ADMIN_LOCKERS, description = "Admin Locker & Box Management APIs")
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_LOCKERS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLockerController {

  private final AdminLockerService adminLockerService;
  private final ResponseHelper responseHelper;

  @Operation(summary = "Get All Lockers", description = "Retrieve all lockers with pagination")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminLockerResponse>>> getAllLockers(Pageable pageable) {
    return ResponseEntity.ok(
        responseHelper.success(adminLockerService.getAllLockers(pageable), "LOCKERS_RETRIEVED"));
  }

  @Operation(
      summary = "Get Lockers By Store",
      description = "Retrieve lockers for a specific store")
  @GetMapping(UriParamConstants.BY_STORE)
  public ResponseEntity<ApiResponse<List<AdminLockerResponse>>> getLockersByStore(
      @PathVariable Long storeId) {
    return ResponseEntity.ok(
        responseHelper.success(adminLockerService.getLockersByStore(storeId), "LOCKERS_RETRIEVED"));
  }

  @Operation(summary = "Get Locker By ID", description = "Retrieve locker details with boxes")
  @GetMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<AdminLockerResponse>> getLockerById(@PathVariable Long id) {
    return ResponseEntity.ok(
        responseHelper.success(adminLockerService.getLockerById(id), "LOCKER_RETRIEVED"));
  }

  @Operation(summary = "Create Locker", description = "Create a new locker")
  @PostMapping
  public ResponseEntity<ApiResponse<AdminLockerResponse>> createLocker(
      @Valid @RequestBody CreateLockerRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(adminLockerService.createLocker(request), "LOCKER_CREATED"));
  }

  @Operation(summary = "Update Locker", description = "Update locker information")
  @PutMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<AdminLockerResponse>> updateLocker(
      @PathVariable Long id, @Valid @RequestBody CreateLockerRequest request) {
    return ResponseEntity.ok(
        responseHelper.success(adminLockerService.updateLocker(id, request), "LOCKER_UPDATED"));
  }

  @Operation(summary = "Set Maintenance Mode", description = "Enable or disable maintenance mode")
  @PutMapping(UriParamConstants.ADMIN_MAINTENANCE)
  public ResponseEntity<ApiResponse<AdminLockerResponse>> setMaintenance(
      @PathVariable Long id, @RequestBody Map<String, Boolean> request) {
    Boolean maintenance = request.get("maintenance");
    return ResponseEntity.ok(
        responseHelper.success(
            adminLockerService.setMaintenance(id, maintenance), "LOCKER_MAINTENANCE_UPDATED"));
  }

  @Operation(summary = "Add Box to Locker", description = "Add a new box to a locker")
  @PostMapping(UriParamConstants.ADMIN_BOXES)
  public ResponseEntity<ApiResponse<AdminLockerResponse>> addBox(
      @PathVariable Long id, @Valid @RequestBody CreateBoxRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(adminLockerService.addBox(id, request), "BOX_ADDED"));
  }

  @Operation(summary = "Update Box Status", description = "Update box status")
  @PutMapping(UriParamConstants.ADMIN_BOX_STATUS)
  public ResponseEntity<ApiResponse<Void>> updateBoxStatus(
      @PathVariable Long boxId, @RequestBody Map<String, String> request) {
    BoxStatus status = BoxStatus.valueOf(request.get("status"));
    adminLockerService.updateBoxStatus(boxId, status);
    return ResponseEntity.ok(responseHelper.success("BOX_STATUS_UPDATED"));
  }

  @Operation(summary = "Delete Locker", description = "Soft delete a locker")
  @DeleteMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<Void>> deleteLocker(@PathVariable Long id) {
    adminLockerService.deleteLocker(id);
    return ResponseEntity.ok(responseHelper.success("LOCKER_DELETED"));
  }
}
