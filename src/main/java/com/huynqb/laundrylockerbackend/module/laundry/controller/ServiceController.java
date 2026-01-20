package com.huynqb.laundrylockerbackend.module.laundry.controller;

import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.module.laundry.dto.response.ServiceResponse;
import com.huynqb.laundrylockerbackend.module.laundry.service.LaundryServiceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for LaundryService operations. */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

  private final LaundryServiceService laundryServiceService;

  /** Get all services. */
  @GetMapping
  public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAllServices() {
    List<ServiceResponse> services = laundryServiceService.getAllServices();
    return ResponseEntity.ok(ApiResponse.success(services, "SERVICES_RETRIEVED"));
  }

  /** Get services by store ID. */
  @GetMapping(params = "storeId")
  public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServicesByStore(
      @RequestParam Long storeId) {
    List<ServiceResponse> services = laundryServiceService.getServicesByStore(storeId);
    return ResponseEntity.ok(ApiResponse.success(services, "SERVICES_RETRIEVED"));
  }

  /** Get service by ID. */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ServiceResponse>> getServiceById(@PathVariable Long id) {
    ServiceResponse service = laundryServiceService.getServiceById(id);
    return ResponseEntity.ok(ApiResponse.success(service, "SERVICE_RETRIEVED"));
  }
}
