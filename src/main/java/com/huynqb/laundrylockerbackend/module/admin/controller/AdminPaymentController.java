package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.service.AdminPaymentService;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentResponse;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for admin payment management. */
@Tag(name = TagConstants.ROOT_TAG_ADMIN_PAYMENTS, description = "Admin Payment Management APIs")
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_PAYMENTS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

  private final AdminPaymentService adminPaymentService;
  private final ResponseHelper responseHelper;

  /** Get all payments with optional status filter. */
  @Operation(
      summary = "Get All Payments",
      description = "Retrieve all payments with optional status filter (Admin only)")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllPayments(
      @RequestParam(required = false) PaymentStatus status, Pageable pageable) {
    Page<PaymentResponse> payments = adminPaymentService.getAllPayments(status, pageable);
    return ResponseEntity.ok(responseHelper.success(payments, "PAYMENTS_RETRIEVED"));
  }

  /** Get payment by ID. */
  @Operation(
      summary = "Get Payment By ID",
      description = "Retrieve payment details by ID (Admin only)")
  @GetMapping(UriParamConstants.ADMIN_PAYMENT_BY_ID)
  public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long paymentId) {
    PaymentResponse payment = adminPaymentService.getPaymentById(paymentId);
    return ResponseEntity.ok(responseHelper.success(payment, "PAYMENT_RETRIEVED"));
  }

  /** Update payment status. */
  @Operation(
      summary = "Update Payment Status",
      description = "Force update payment status (Admin override)")
  @PutMapping("/{paymentId}/status")
  public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
      @PathVariable Long paymentId, @RequestParam PaymentStatus status) {
    PaymentResponse payment = adminPaymentService.updatePaymentStatus(paymentId, status);
    return ResponseEntity.ok(responseHelper.success(payment, "PAYMENT_STATUS_UPDATED"));
  }
}
