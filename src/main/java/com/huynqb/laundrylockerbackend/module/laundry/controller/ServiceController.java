package com.huynqb.laundrylockerbackend.module.laundry.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.laundry.dto.response.ServiceResponse;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceCategory;
import com.huynqb.laundrylockerbackend.module.laundry.service.LaundryServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for LaundryService operations. */
@Tag(name = TagConstants.ROOT_TAG_SERVICES)
@RequestMapping(UriParamConstants.ROOT_URI_SERVICES)
@RestController
@RequiredArgsConstructor
public class ServiceController {

  private final LaundryServiceService laundryServiceService;
  private final ResponseHelper responseHelper;

  /** Get all services. */
  @Operation(summary = "Get All Services", description = "Retrieve all laundry services")
  @GetMapping
  public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAllServices() {
    List<ServiceResponse> services = laundryServiceService.getAllServices();
    return ResponseEntity.ok(responseHelper.success(services, "SERVICES_RETRIEVED"));
  }

  /** Get services by store ID. */
  @Operation(
      summary = "Get Services By Store",
      description = "Retrieve services available at a specific store")
  @GetMapping(params = "storeId")
  public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServicesByStore(
      @RequestParam Long storeId) {
    List<ServiceResponse> services = laundryServiceService.getServicesByStore(storeId);
    return ResponseEntity.ok(responseHelper.success(services, "SERVICES_RETRIEVED"));
  }

  /**
   * Get services by category (STORAGE or LAUNDRY). This is the main endpoint for the mobile app to
   * show parent categories and their child services.
   */
  @Operation(
      summary = "Get Services By Category",
      description =
          "Retrieve services by category. STORAGE = Dịch vụ gửi đồ, LAUNDRY = Dịch vụ giặt")
  @GetMapping(params = "category")
  public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServicesByCategory(
      @RequestParam ServiceCategory category) {
    List<ServiceResponse> services = laundryServiceService.getServicesByCategory(category);
    return ResponseEntity.ok(responseHelper.success(services, "SERVICES_RETRIEVED"));
  }

  /** Get services by store ID and category. */
  @Operation(
      summary = "Get Services By Store and Category",
      description = "Retrieve services at a specific store filtered by category")
  @GetMapping(params = {"storeId", "category"})
  public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServicesByStoreAndCategory(
      @RequestParam Long storeId, @RequestParam ServiceCategory category) {
    List<ServiceResponse> services =
        laundryServiceService.getServicesByStoreAndCategory(storeId, category);
    return ResponseEntity.ok(responseHelper.success(services, "SERVICES_RETRIEVED"));
  }

  /**
   * Get services by locker ID. This endpoint is designed for Kiosk app which only knows the Locker
   * ID from environment config.
   */
  @Operation(
      summary = "Get Services By Locker",
      description =
          "Retrieve services available at the store where the locker is located. "
              + "Designed for Kiosk app that only has lockerId from environment config.")
  @GetMapping(params = "lockerId")
  public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServicesByLocker(
      @RequestParam Long lockerId) {
    List<ServiceResponse> services = laundryServiceService.getServicesByLocker(lockerId);
    return ResponseEntity.ok(responseHelper.success(services, "SERVICES_RETRIEVED"));
  }

  /**
   * Get services by locker ID and category. This endpoint is designed for Kiosk app to get services
   * filtered by category (STORAGE or LAUNDRY).
   */
  @Operation(
      summary = "Get Services By Locker and Category",
      description =
          "Retrieve services at a locker's store filtered by category. "
              + "Designed for Kiosk app. Category: STORAGE = Dịch vụ gửi đồ, LAUNDRY = Dịch vụ giặt")
  @GetMapping(params = {"lockerId", "category"})
  public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServicesByLockerAndCategory(
      @RequestParam Long lockerId, @RequestParam ServiceCategory category) {
    List<ServiceResponse> services =
        laundryServiceService.getServicesByLockerAndCategory(lockerId, category);
    return ResponseEntity.ok(responseHelper.success(services, "SERVICES_RETRIEVED"));
  }

  /** Get service by ID. */
  @Operation(summary = "Get Service By ID", description = "Retrieve service details by ID")
  @GetMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<ServiceResponse>> getServiceById(@PathVariable Long id) {
    ServiceResponse service = laundryServiceService.getServiceById(id);
    return ResponseEntity.ok(responseHelper.success(service, "SERVICE_RETRIEVED"));
  }
}
