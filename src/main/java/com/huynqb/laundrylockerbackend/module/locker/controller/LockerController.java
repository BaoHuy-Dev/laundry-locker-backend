package com.huynqb.laundrylockerbackend.module.locker.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.locker.dto.request.LockerReportRequest;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.BoxResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerReportResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerResponse;
import com.huynqb.laundrylockerbackend.module.locker.service.LockerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Locker and Box operations. */
@Tag(name = TagConstants.ROOT_TAG_LOCKERS)
@RequestMapping(UriParamConstants.ROOT_URI_LOCKERS)
@RestController
@RequiredArgsConstructor
public class LockerController {

  private final LockerService lockerService;
  private final ResponseHelper responseHelper;
  private final JwtTokenProvider jwtTokenProvider;

  /** Get all lockers. */
  @Operation(summary = "Get All Lockers", description = "Retrieve all active lockers")
  @GetMapping
  public ResponseEntity<ApiResponse<List<LockerResponse>>> getAllLockers() {
    List<LockerResponse> lockers = lockerService.getAllLockers();
    return ResponseEntity.ok(responseHelper.success(lockers, "LOCKERS_RETRIEVED"));
  }

  /** Get lockers by store ID. */
  @Operation(summary = "Get Lockers By Store", description = "Retrieve lockers at a specific store")
  @GetMapping(params = "storeId")
  public ResponseEntity<ApiResponse<List<LockerResponse>>> getLockersByStore(
      @RequestParam Long storeId) {
    List<LockerResponse> lockers = lockerService.getLockersByStore(storeId);
    return ResponseEntity.ok(responseHelper.success(lockers, "LOCKERS_RETRIEVED"));
  }

  /** Get locker by ID with boxes. */
  @Operation(summary = "Get Locker By ID", description = "Retrieve locker details with boxes by ID")
  @GetMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<LockerResponse>> getLockerById(@PathVariable Long id) {
    LockerResponse locker = lockerService.getLockerById(id);
    return ResponseEntity.ok(responseHelper.success(locker, "LOCKER_RETRIEVED"));
  }

  /** Get all boxes in a locker. */
  @Operation(
      summary = "Get Boxes By Locker",
      description = "Retrieve all boxes in a specific locker")
  @GetMapping(UriParamConstants.BOXES_BY_LOCKER)
  public ResponseEntity<ApiResponse<List<BoxResponse>>> getBoxesByLocker(@PathVariable Long id) {
    List<BoxResponse> boxes = lockerService.getBoxesByLocker(id);
    return ResponseEntity.ok(responseHelper.success(boxes, "BOXES_RETRIEVED"));
  }

  /** Get available boxes in a locker. */
  @Operation(
      summary = "Get Available Boxes",
      description = "Retrieve available boxes in a specific locker")
  @GetMapping(UriParamConstants.BOXES_AVAILABLE)
  public ResponseEntity<ApiResponse<List<BoxResponse>>> getAvailableBoxes(@PathVariable Long id) {
    List<BoxResponse> boxes = lockerService.getAvailableBoxes(id);
    return ResponseEntity.ok(responseHelper.success(boxes, "BOXES_RETRIEVED"));
  }

  @Operation(summary = "Report Locker", description = "Report a broken locker")
  @PostMapping("/{id}/report")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<LockerReportResponse>> reportLocker(
      @PathVariable Long id,
      @Valid @RequestBody LockerReportRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    LockerReportResponse response = lockerService.reportLocker(id, request, userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(response, "LOCKER_REPORTED"));
  }

  /** Get locker reports submitted by current user. */
  @Operation(
      summary = "Get My Locker Reports",
      description = "Get all locker reports submitted by the current user")
  @GetMapping("/my-reports")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<LockerReportResponse>>> getMyReports(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    List<LockerReportResponse> reports = lockerService.getUserReports(userId);
    return ResponseEntity.ok(responseHelper.success(reports, "REPORTS_RETRIEVED"));
  }

  private Long extractUserId(String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return jwtTokenProvider.getUserIdFromToken(token);
  }
}
