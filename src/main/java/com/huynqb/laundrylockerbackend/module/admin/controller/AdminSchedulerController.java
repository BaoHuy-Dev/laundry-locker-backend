package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.scheduler.OrderSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin controller for managing scheduler jobs. Allows manual triggering of scheduled tasks for
 * testing/debugging.
 */
@Tag(name = TagConstants.ROOT_TAG_ADMIN_SCHEDULER, description = "Admin Scheduler Management APIs")
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_SCHEDULER)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSchedulerController {

  private final OrderSchedulerService orderSchedulerService;
  private final ResponseHelper responseHelper;

  /** Manually trigger auto-cancel job for unconfirmed orders. */
  @Operation(
      summary = "Trigger Auto-Cancel Job",
      description = "Manually trigger the job that auto-cancels unconfirmed orders")
  @PostMapping(UriParamConstants.SCHEDULER_AUTO_CANCEL)
  public ResponseEntity<ApiResponse<Map<String, Object>>> triggerAutoCancelJob() {
    int count = orderSchedulerService.triggerAutoCancelJob();

    Map<String, Object> result = new HashMap<>();
    result.put("jobName", "auto-cancel-unconfirmed-orders");
    result.put("status", "completed");
    result.put("message", "Auto-cancel job executed successfully");

    return ResponseEntity.ok(responseHelper.success(result, "JOB_EXECUTED"));
  }

  /** Manually trigger box release job. */
  @Operation(
      summary = "Trigger Box Release Job",
      description = "Manually trigger the job that releases boxes from completed orders")
  @PostMapping(UriParamConstants.SCHEDULER_RELEASE_BOXES)
  public ResponseEntity<ApiResponse<Map<String, Object>>> triggerBoxReleaseJob() {
    orderSchedulerService.triggerBoxReleaseJob();

    Map<String, Object> result = new HashMap<>();
    result.put("jobName", "release-boxes-after-completion");
    result.put("status", "completed");
    result.put("message", "Box release job executed successfully");

    return ResponseEntity.ok(responseHelper.success(result, "JOB_EXECUTED"));
  }

  /** Manually trigger pickup reminder job. */
  @Operation(
      summary = "Trigger Pickup Reminder Job",
      description = "Manually trigger the job that sends pickup reminders")
  @PostMapping(UriParamConstants.SCHEDULER_PICKUP_REMINDERS)
  public ResponseEntity<ApiResponse<Map<String, Object>>> triggerPickupReminderJob() {
    orderSchedulerService.sendPickupReminders();

    Map<String, Object> result = new HashMap<>();
    result.put("jobName", "send-pickup-reminders");
    result.put("status", "completed");
    result.put("message", "Pickup reminder job executed successfully");

    return ResponseEntity.ok(responseHelper.success(result, "JOB_EXECUTED"));
  }

  /** Get scheduler job status and configuration. */
  @Operation(
      summary = "Get Scheduler Status",
      description = "Get current scheduler configuration and status")
  @GetMapping(UriParamConstants.SCHEDULER_STATUS)
  public ResponseEntity<ApiResponse<Map<String, Object>>> getSchedulerStatus() {
    Map<String, Object> status = new HashMap<>();
    status.put("schedulerEnabled", true);
    status.put(
        "jobs",
        new String[] {
          "auto-cancel-unconfirmed-orders",
          "release-boxes-after-completion",
          "send-pickup-reminders"
        });
    status.put("message", "Scheduler is running");

    return ResponseEntity.ok(responseHelper.success(status, "SCHEDULER_STATUS"));
  }
}
