package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.dto.UpdateImageRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateServiceRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.UpdateUserStatusRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminServiceResponse;
import com.huynqb.laundrylockerbackend.module.admin.service.AdminServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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

@Tag(name = TagConstants.ROOT_TAG_ADMIN_SERVICES, description = "Admin Service Management APIs")
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_SERVICES)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceController {

  private final AdminServiceService adminServiceService;
  private final ResponseHelper responseHelper;

  @Operation(summary = "Get All Services", description = "Retrieve all services with pagination")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminServiceResponse>>> getAllServices(Pageable pageable) {
    return ResponseEntity.ok(
        responseHelper.success(adminServiceService.getAllServices(pageable), "SERVICES_RETRIEVED"));
  }

  @Operation(summary = "Get Service By ID", description = "Retrieve service details")
  @GetMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<AdminServiceResponse>> getServiceById(@PathVariable Long id) {
    return ResponseEntity.ok(
        responseHelper.success(adminServiceService.getServiceById(id), "SERVICE_RETRIEVED"));
  }

  @Operation(summary = "Create Service", description = "Create a new laundry service")
  @PostMapping
  public ResponseEntity<ApiResponse<AdminServiceResponse>> createService(
      @Valid @RequestBody CreateServiceRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            responseHelper.success(adminServiceService.createService(request), "SERVICE_CREATED"));
  }

  @Operation(summary = "Update Service", description = "Update service information")
  @PutMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<AdminServiceResponse>> updateService(
      @PathVariable Long id, @Valid @RequestBody CreateServiceRequest request) {
    return ResponseEntity.ok(
        responseHelper.success(adminServiceService.updateService(id, request), "SERVICE_UPDATED"));
  }

  @Operation(summary = "Update Price", description = "Update service price only")
  @PutMapping(UriParamConstants.ADMIN_PRICE)
  public ResponseEntity<ApiResponse<AdminServiceResponse>> updatePrice(
      @PathVariable Long id, @RequestBody Map<String, BigDecimal> request) {
    BigDecimal price = request.get("price");
    return ResponseEntity.ok(
        responseHelper.success(
            adminServiceService.updatePrice(id, price), "SERVICE_PRICE_UPDATED"));
  }

  @Operation(summary = "Update Status", description = "Activate or deactivate a service")
  @PutMapping(UriParamConstants.ADMIN_STATUS)
  public ResponseEntity<ApiResponse<AdminServiceResponse>> updateStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
    return ResponseEntity.ok(
        responseHelper.success(
            adminServiceService.updateStatus(id, request.getEnabled()), "SERVICE_STATUS_UPDATED"));
  }

  @Operation(summary = "Update Service Image", description = "Update service image URL")
  @PutMapping(UriParamConstants.ADMIN_IMAGE)
  public ResponseEntity<ApiResponse<AdminServiceResponse>> updateServiceImage(
      @PathVariable Long id, @Valid @RequestBody UpdateImageRequest request) {
    return ResponseEntity.ok(
        responseHelper.success(
            adminServiceService.updateServiceImage(id, request.getImageUrl()),
            "SERVICE_IMAGE_UPDATED"));
  }

  @Operation(summary = "Delete Service", description = "Soft delete a service")
  @DeleteMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
    adminServiceService.deleteService(id);
    return ResponseEntity.ok(responseHelper.success("SERVICE_DELETED"));
  }
}
