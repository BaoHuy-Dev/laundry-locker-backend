package com.huynqb.laundrylockerbackend.module.locker.controller;

import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.BoxResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerResponse;
import com.huynqb.laundrylockerbackend.module.locker.service.LockerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Locker and Box operations. */
@RestController
@RequestMapping("/api/lockers")
@RequiredArgsConstructor
public class LockerController {

  private final LockerService lockerService;

  /** Get all lockers. */
  @GetMapping
  public ResponseEntity<ApiResponse<List<LockerResponse>>> getAllLockers() {
    List<LockerResponse> lockers = lockerService.getAllLockers();
    return ResponseEntity.ok(ApiResponse.success(lockers, "LOCKERS_RETRIEVED"));
  }

  /** Get lockers by store ID. */
  @GetMapping(params = "storeId")
  public ResponseEntity<ApiResponse<List<LockerResponse>>> getLockersByStore(
      @RequestParam Long storeId) {
    List<LockerResponse> lockers = lockerService.getLockersByStore(storeId);
    return ResponseEntity.ok(ApiResponse.success(lockers, "LOCKERS_RETRIEVED"));
  }

  /** Get locker by ID with boxes. */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<LockerResponse>> getLockerById(@PathVariable Long id) {
    LockerResponse locker = lockerService.getLockerById(id);
    return ResponseEntity.ok(ApiResponse.success(locker, "LOCKER_RETRIEVED"));
  }

  /** Get all boxes in a locker. */
  @GetMapping("/{id}/boxes")
  public ResponseEntity<ApiResponse<List<BoxResponse>>> getBoxesByLocker(@PathVariable Long id) {
    List<BoxResponse> boxes = lockerService.getBoxesByLocker(id);
    return ResponseEntity.ok(ApiResponse.success(boxes, "BOXES_RETRIEVED"));
  }

  /** Get available boxes in a locker. */
  @GetMapping("/{id}/boxes/available")
  public ResponseEntity<ApiResponse<List<BoxResponse>>> getAvailableBoxes(@PathVariable Long id) {
    List<BoxResponse> boxes = lockerService.getAvailableBoxes(id);
    return ResponseEntity.ok(ApiResponse.success(boxes, "BOXES_RETRIEVED"));
  }
}
