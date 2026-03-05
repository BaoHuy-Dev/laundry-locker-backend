package com.huynqb.laundrylockerbackend.module.iot.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.BoxStatusUpdateRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.PickupRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.UnlockBoxRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.request.VerifyPinRequest;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.PickupResponse;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.UnlockBoxResponse;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.VerifyPinResponse;
import com.huynqb.laundrylockerbackend.module.iot.service.IoTService;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.StaffCodeUnlockRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.StaffCodeUnlockResponse;
import com.huynqb.laundrylockerbackend.module.partner.service.StaffAccessCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for IoT operations. Handles locker control, PIN verification, and pickup
 * confirmation.
 */
@Tag(name = TagConstants.ROOT_TAG_IOT, description = "IoT Locker Control APIs")
@RequestMapping(UriParamConstants.ROOT_URI_IOT)
@RestController
@RequiredArgsConstructor
public class IoTController {

  private final IoTService ioTService;
  private final StaffAccessCodeService staffAccessCodeService;
  private final JwtTokenProvider jwtTokenProvider;
  private final ResponseHelper responseHelper;
  private final com.huynqb.laundrylockerbackend.module.iot.service.LockerMqttService
      lockerMqttService;

  /** Verify PIN code for a box. */
  @Operation(
      summary = "Verify PIN",
      description = "Verify if a PIN code is valid for a specific box")
  @PostMapping(UriParamConstants.IOT_VERIFY_PIN)
  public ResponseEntity<ApiResponse<VerifyPinResponse>> verifyPin(
      @Valid @RequestBody VerifyPinRequest request) {
    VerifyPinResponse response = ioTService.verifyPin(request);
    String code = response.getValid() ? "PIN_VALID" : "PIN_INVALID";
    return ResponseEntity.ok(responseHelper.success(response, code));
  }

  /** Unlock a box using PIN code. */
  @Operation(
      summary = "Unlock Box",
      description = "Unlock a locker box using PIN code. Returns unlock token for IoT device.")
  @PostMapping(UriParamConstants.IOT_UNLOCK)
  public ResponseEntity<ApiResponse<UnlockBoxResponse>> unlockBox(
      @Valid @RequestBody UnlockBoxRequest request) {
    UnlockBoxResponse response = ioTService.unlockBox(request);
    String code = response.getSuccess() ? "BOX_UNLOCKED" : "UNLOCK_FAILED";
    return ResponseEntity.ok(responseHelper.success(response, code));
  }

  /** Confirm customer pickup - completes the order. */
  @Operation(
      summary = "Confirm Pickup",
      description = "Customer confirms pickup, completing the order and releasing the box")
  @PostMapping(UriParamConstants.IOT_PICKUP)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PickupResponse>> confirmPickup(
      @Valid @RequestBody PickupRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    PickupResponse response = ioTService.confirmPickup(request, userId);
    String code = response.getSuccess() ? "PICKUP_CONFIRMED" : "PICKUP_FAILED";
    return ResponseEntity.ok(responseHelper.success(response, code));
  }

  /** Update box status from IoT device/sensor. */
  @Operation(
      summary = "Update Box Status",
      description = "Update box status from IoT device or sensor (internal use)")
  @PostMapping(UriParamConstants.IOT_BOX_STATUS)
  public ResponseEntity<ApiResponse<Void>> updateBoxStatus(
      @Valid @RequestBody BoxStatusUpdateRequest request) {
    ioTService.updateBoxStatus(request);
    return ResponseEntity.ok(responseHelper.success("BOX_STATUS_UPDATED"));
  }

  /** Unlock box using staff access code (for physical staff without account). */
  @Operation(
      summary = "Unlock Box with Staff Code",
      description =
          "Unlock a box using staff access code. Used by physical staff who don't have system accounts.")
  @PostMapping(UriParamConstants.IOT_UNLOCK_WITH_CODE)
  public ResponseEntity<ApiResponse<StaffCodeUnlockResponse>> unlockWithCode(
      @Valid @RequestBody StaffCodeUnlockRequest request) {
    StaffCodeUnlockResponse response = staffAccessCodeService.unlockWithCode(request);
    String code = response.getSuccess() ? "BOX_UNLOCKED" : "UNLOCK_FAILED";
    return ResponseEntity.ok(responseHelper.success(response, code));
  }

  private Long extractUserId(String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return jwtTokenProvider.getUserIdFromToken(token);
  }

  @org.springframework.web.bind.annotation.GetMapping("/test-mqtt")
  public ResponseEntity<String> testMqtt() {
    try {
      lockerMqttService.sendUnlockCommand("LOC-01-001", 1);
      return ResponseEntity.ok("Successfully sent MQTT command!");
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(
              "MQTT Error: "
                  + e.getMessage()
                  + " | Cause: "
                  + (e.getCause() != null ? e.getCause().getMessage() : "none"));
    }
  }
}
