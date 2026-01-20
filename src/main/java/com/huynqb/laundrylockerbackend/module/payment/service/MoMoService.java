package com.huynqb.laundrylockerbackend.module.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.payment.config.MoMoConfig;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentUrlResponse;
import com.huynqb.laundrylockerbackend.module.payment.exception.PaymentException;
import com.huynqb.laundrylockerbackend.module.payment.util.PaymentUtils;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MoMo payment service. Handles MoMo payment URL creation and callback processing. Follows SRP by
 * focusing only on MoMo-specific operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoService {

  private final MoMoConfig momoConfig;
  private final ObjectMapper objectMapper;

  private static final HttpClient httpClient =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

  /** MoMo result codes */
  private static final int RESULT_CODE_SUCCESS = 0;

  /**
   * Create MoMo payment URL.
   *
   * @param order Order to pay
   * @return PaymentUrlResponse with payment URL and QR
   */
  public PaymentUrlResponse createPaymentUrl(Order order) {
    log.info("Creating MoMo payment URL for order: {}", order.getId());

    String requestId = UUID.randomUUID().toString();
    String orderId = PaymentUtils.generateTxnRef(order.getId());
    long amount = order.getTotalPrice().longValue();
    String orderInfo = "Thanh toan don hang " + order.getId();
    String extraData = "";

    // Build raw signature
    String rawSignature =
        String.format(
            "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
            momoConfig.getAccessKey(),
            amount,
            extraData,
            momoConfig.getIpnUrl(),
            orderId,
            orderInfo,
            momoConfig.getPartnerCode(),
            momoConfig.getRedirectUrl(),
            requestId,
            momoConfig.getRequestType());

    // Create HMAC SHA256 signature
    String signature = PaymentUtils.hmacSHA256(momoConfig.getSecretKey(), rawSignature);

    // Build request body
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("partnerCode", momoConfig.getPartnerCode());
    requestBody.put("partnerName", "Laundry Locker");
    requestBody.put("storeId", "LaundryLocker01");
    requestBody.put("requestId", requestId);
    requestBody.put("amount", amount);
    requestBody.put("orderId", orderId);
    requestBody.put("orderInfo", orderInfo);
    requestBody.put("redirectUrl", momoConfig.getRedirectUrl());
    requestBody.put("ipnUrl", momoConfig.getIpnUrl());
    requestBody.put("lang", "vi");
    requestBody.put("extraData", extraData);
    requestBody.put("requestType", momoConfig.getRequestType());
    requestBody.put("signature", signature);

    try {
      String jsonBody = objectMapper.writeValueAsString(requestBody);
      log.debug("MoMo request body: {}", jsonBody);

      // Send request to MoMo
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(momoConfig.getEndpoint()))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      log.debug("MoMo response: {}", response.body());

      // Parse response
      @SuppressWarnings("unchecked")
      Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);

      int resultCode = (int) responseMap.get("resultCode");
      if (resultCode != RESULT_CODE_SUCCESS) {
        String message = (String) responseMap.get("message");
        log.error("MoMo payment creation failed: {} - {}", resultCode, message);
        throw new PaymentException("E_PAYMENT005", "MoMo error: " + message);
      }

      LocalDateTime expireAt = LocalDateTime.now().plusMinutes(momoConfig.getExpireMinutes());

      return PaymentUrlResponse.builder()
          .orderId(order.getId())
          .paymentUrl((String) responseMap.get("payUrl"))
          .qrCodeUrl((String) responseMap.get("qrCodeUrl"))
          .deeplink((String) responseMap.get("deeplink"))
          .expireAt(expireAt)
          .build();

    } catch (PaymentException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error creating MoMo payment", e);
      throw new PaymentException("E_PAYMENT005", "Failed to create MoMo payment", e);
    }
  }

  /**
   * Verify MoMo callback signature.
   *
   * @param params Callback parameters
   * @return true if signature is valid
   */
  public boolean verifySignature(Map<String, Object> params) {
    String receivedSignature = (String) params.get("signature");
    if (receivedSignature == null || receivedSignature.isEmpty()) {
      log.warn("Missing signature in MoMo callback");
      return false;
    }

    // Build raw signature
    String rawSignature =
        String.format(
            "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
            momoConfig.getAccessKey(),
            params.get("amount"),
            params.get("extraData"),
            params.get("message"),
            params.get("orderId"),
            params.get("orderInfo"),
            params.get("orderType"),
            params.get("partnerCode"),
            params.get("payType"),
            params.get("requestId"),
            params.get("responseTime"),
            params.get("resultCode"),
            params.get("transId"));

    String calculatedSignature = PaymentUtils.hmacSHA256(momoConfig.getSecretKey(), rawSignature);

    boolean isValid = calculatedSignature.equals(receivedSignature);
    if (!isValid) {
      log.warn("Invalid MoMo signature");
    }

    return isValid;
  }

  /**
   * Process MoMo callback.
   *
   * @param params Callback parameters
   * @return true if payment was successful
   */
  public boolean isPaymentSuccess(Map<String, Object> params) {
    int resultCode = (int) params.get("resultCode");
    return resultCode == RESULT_CODE_SUCCESS;
  }

  /**
   * Extract order ID from MoMo callback.
   *
   * @param params Callback parameters
   * @return Order ID
   */
  public Long extractOrderId(Map<String, Object> params) {
    String orderId = (String) params.get("orderId");
    return PaymentUtils.extractOrderIdFromTxnRef(orderId);
  }

  /**
   * Get MoMo transaction ID from callback.
   *
   * @param params Callback parameters
   * @return Transaction ID
   */
  public String getTransactionId(Map<String, Object> params) {
    Object transId = params.get("transId");
    return transId != null ? String.valueOf(transId) : null;
  }
}
