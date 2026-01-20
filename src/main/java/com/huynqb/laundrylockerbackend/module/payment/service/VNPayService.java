package com.huynqb.laundrylockerbackend.module.payment.service;

import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.payment.config.VNPayConfig;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentUrlResponse;
import com.huynqb.laundrylockerbackend.module.payment.exception.PaymentException;
import com.huynqb.laundrylockerbackend.module.payment.util.PaymentUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * VNPay payment service. Handles VNPay payment URL creation and callback processing. Follows SRP by
 * focusing only on VNPay-specific operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService {

  private final VNPayConfig vnPayConfig;

  /** VNPay response codes */
  private static final String RESPONSE_CODE_SUCCESS = "00";

  /**
   * Create VNPay payment URL.
   *
   * @param order Order to pay
   * @param ipAddress Client IP address
   * @param bankCode Optional bank code
   * @param language Payment page language (vn/en)
   * @return PaymentUrlResponse with payment URL
   */
  public PaymentUrlResponse createPaymentUrl(
      Order order, String ipAddress, String bankCode, String language) {
    log.info("Creating VNPay payment URL for order: {}", order.getId());

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expireDate = now.plusMinutes(vnPayConfig.getExpireMinutes());
    String txnRef = PaymentUtils.generateTxnRef(order.getId());

    // Convert amount to VNPay format (multiply by 100 to remove decimals)
    long amount = order.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue();

    // Build payment parameters
    Map<String, String> params = new HashMap<>();
    params.put("vnp_Version", vnPayConfig.getVersion());
    params.put("vnp_Command", vnPayConfig.getCommand());
    params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
    params.put("vnp_Amount", String.valueOf(amount));
    params.put("vnp_CurrCode", vnPayConfig.getCurrCode());
    params.put("vnp_TxnRef", txnRef);
    params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
    params.put("vnp_OrderType", "other");
    params.put("vnp_Locale", language != null ? language : "vn");
    params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
    params.put("vnp_IpAddr", ipAddress);
    params.put("vnp_CreateDate", PaymentUtils.formatVnPayDate(now));
    params.put("vnp_ExpireDate", PaymentUtils.formatVnPayDate(expireDate));

    // Add bank code if provided
    if (bankCode != null && !bankCode.isEmpty()) {
      params.put("vnp_BankCode", bankCode);
    }

    // Build hash data (sorted alphabetically)
    String hashData = PaymentUtils.buildQueryString(params, true);

    // Create secure hash
    String secureHash = PaymentUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
    params.put("vnp_SecureHash", secureHash);

    // Build final URL
    String queryString = PaymentUtils.buildQueryString(params, true);
    String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryString;

    log.info("VNPay payment URL created for order: {}, txnRef: {}", order.getId(), txnRef);

    return PaymentUrlResponse.builder()
        .orderId(order.getId())
        .paymentUrl(paymentUrl)
        .expireAt(expireDate)
        .build();
  }

  /**
   * Verify VNPay callback checksum.
   *
   * @param params Callback parameters
   * @return true if checksum is valid
   */
  public boolean verifyChecksum(Map<String, String> params) {
    String receivedHash = params.get("vnp_SecureHash");
    if (receivedHash == null || receivedHash.isEmpty()) {
      log.warn("Missing vnp_SecureHash in callback");
      return false;
    }

    // Remove hash params before verification
    Map<String, String> verifyParams = new TreeMap<>(params);
    verifyParams.remove("vnp_SecureHash");
    verifyParams.remove("vnp_SecureHashType");

    // Build hash data
    String hashData = PaymentUtils.buildQueryString(verifyParams, true);
    String calculatedHash = PaymentUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

    boolean isValid = calculatedHash.equalsIgnoreCase(receivedHash);
    if (!isValid) {
      log.warn("Invalid VNPay checksum. Expected: {}, Received: {}", calculatedHash, receivedHash);
    }

    return isValid;
  }

  /**
   * Process VNPay IPN callback.
   *
   * @param params Callback parameters
   * @return IPN response map (RspCode, Message)
   */
  public Map<String, String> processIpnCallback(Map<String, String> params) {
    log.info("Processing VNPay IPN callback: txnRef={}", params.get("vnp_TxnRef"));

    // Verify checksum first
    if (!verifyChecksum(params)) {
      log.error("Invalid VNPay checksum");
      return createIpnResponse("97", "Invalid checksum");
    }

    String responseCode = params.get("vnp_ResponseCode");
    String transactionStatus = params.get("vnp_TransactionStatus");
    String txnRef = params.get("vnp_TxnRef");

    log.info(
        "VNPay IPN: txnRef={}, responseCode={}, status={}",
        txnRef,
        responseCode,
        transactionStatus);

    // Return success to VNPay
    return createIpnResponse("00", "Success");
  }

  /**
   * Process VNPay return URL callback.
   *
   * @param params Return URL parameters
   * @return true if payment was successful
   */
  public boolean isPaymentSuccess(Map<String, String> params) {
    if (!verifyChecksum(params)) {
      throw new PaymentException("E_PAYMENT002", "Invalid payment signature");
    }

    String responseCode = params.get("vnp_ResponseCode");
    String transactionStatus = params.get("vnp_TransactionStatus");

    return RESPONSE_CODE_SUCCESS.equals(responseCode)
        && RESPONSE_CODE_SUCCESS.equals(transactionStatus);
  }

  /**
   * Extract transaction reference from callback params.
   *
   * @param params Callback parameters
   * @return Transaction reference
   */
  public String getTxnRef(Map<String, String> params) {
    return params.get("vnp_TxnRef");
  }

  /**
   * Extract VNPay transaction number from callback params.
   *
   * @param params Callback parameters
   * @return VNPay transaction number
   */
  public String getTransactionNo(Map<String, String> params) {
    return params.get("vnp_TransactionNo");
  }

  /**
   * Get VNPay response message.
   *
   * @param responseCode VNPay response code
   * @return Human-readable message
   */
  public String getResponseMessage(String responseCode) {
    return switch (responseCode) {
      case "00" -> "Giao dịch thành công";
      case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ";
      case "09" -> "Thẻ/Tài khoản chưa đăng ký Internet Banking";
      case "10" -> "Xác thực thông tin thẻ không đúng quá 3 lần";
      case "11" -> "Đã hết hạn chờ thanh toán";
      case "12" -> "Thẻ/Tài khoản bị khóa";
      case "13" -> "Nhập sai OTP";
      case "24" -> "Giao dịch bị hủy";
      case "51" -> "Tài khoản không đủ số dư";
      case "65" -> "Tài khoản vượt quá hạn mức";
      case "75" -> "Ngân hàng thanh toán đang bảo trì";
      case "79" -> "Nhập sai mật khẩu quá số lần quy định";
      case "99" -> "Lỗi không xác định";
      default -> "Lỗi không xác định: " + responseCode;
    };
  }

  private Map<String, String> createIpnResponse(String rspCode, String message) {
    Map<String, String> response = new HashMap<>();
    response.put("RspCode", rspCode);
    response.put("Message", message);
    return response;
  }
}
