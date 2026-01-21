package com.huynqb.laundrylockerbackend.module.payment.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.payment.dto.request.CreatePaymentRequest;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentResponse;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentUrlResponse;
import com.huynqb.laundrylockerbackend.module.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment operations. Handles online payment creation and callback processing.
 */
@Slf4j
@Tag(name = TagConstants.ROOT_TAG_PAYMENTS)
@RequestMapping(UriParamConstants.ROOT_URI_PAYMENTS)
@RestController
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;
  private final ResponseHelper responseHelper;

  /** Create online payment and get payment URL. */
  @Operation(
      summary = "Create Payment",
      description = "Create online payment and get redirect URL for VNPay/MoMo")
  @PostMapping("/create")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PaymentUrlResponse>> createPayment(
      @Valid @RequestBody CreatePaymentRequest request, HttpServletRequest servletRequest) {
    String ipAddress = getClientIpAddress(servletRequest);
    PaymentUrlResponse response = paymentService.createPayment(request, ipAddress);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(response, "PAYMENT_CREATED"));
  }

  /** VNPay IPN callback (server-to-server). */
  @Operation(summary = "VNPay IPN", description = "VNPay Instant Payment Notification callback")
  @GetMapping("/vnpay/ipn")
  public ResponseEntity<Map<String, String>> vnPayIpn(@RequestParam Map<String, String> params) {
    log.info("Received VNPay IPN callback");
    Map<String, String> response = paymentService.processVNPayIpn(params);
    return ResponseEntity.ok(response);
  }

  /** VNPay return URL (redirect from payment page). */
  @Operation(summary = "VNPay Return", description = "VNPay return URL after payment completion")
  @GetMapping("/vnpay/return")
  public ResponseEntity<ApiResponse<PaymentResponse>> vnPayReturn(
      @RequestParam Map<String, String> params) {
    log.info("Received VNPay return redirect");
    PaymentResponse response = paymentService.processVNPayReturn(params);
    return ResponseEntity.ok(responseHelper.success(response, "PAYMENT_SUCCESS"));
  }

  /** MoMo callback. */
  @Operation(summary = "MoMo Callback", description = "MoMo payment callback")
  @PostMapping("/momo/callback")
  public ResponseEntity<Map<String, Object>> momoCallback(@RequestBody Map<String, Object> params) {
    log.info("Received MoMo callback");
    paymentService.processMoMoCallback(params);

    Map<String, Object> response = new HashMap<>();
    response.put("status", 0);
    response.put("message", "Success");
    return ResponseEntity.ok(response);
  }

  /** MoMo return URL. */
  @Operation(summary = "MoMo Return", description = "MoMo return URL after payment")
  @GetMapping("/momo/return")
  public ResponseEntity<ApiResponse<String>> momoReturn(@RequestParam Map<String, String> params) {
    log.info("Received MoMo return redirect");
    // This endpoint is for user redirect, actual processing happens via callback
    return ResponseEntity.ok(responseHelper.success("Payment completed", "PAYMENT_SUCCESS"));
  }

  /** Get payment by ID. */
  @Operation(summary = "Get Payment", description = "Get payment details by ID")
  @GetMapping("/{paymentId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long paymentId) {
    PaymentResponse response = paymentService.getPaymentById(paymentId);
    return ResponseEntity.ok(responseHelper.success(response, "PAYMENT_RETRIEVED"));
  }

  /** Get payments by order ID. */
  @Operation(summary = "Get Payments By Order", description = "Get all payments for an order")
  @GetMapping("/order/{orderId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrder(
      @PathVariable Long orderId) {
    List<PaymentResponse> response = paymentService.getPaymentsByOrder(orderId);
    return ResponseEntity.ok(responseHelper.success(response, "PAYMENT_RETRIEVED"));
  }

  /**
   * Get client IP address.
   *
   * @param request HTTP request
   * @return Client IP address
   */
  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }
}
