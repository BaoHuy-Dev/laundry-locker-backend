package com.huynqb.laundrylockerbackend.module.payment.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for payment operations. Follows DRY principle by centralizing common operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentUtils {

  private static final String HMAC_SHA512 = "HmacSHA512";
  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final DateTimeFormatter VN_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  /**
   * Generate HMAC SHA512 hash for VNPay.
   *
   * @param key Secret key
   * @param data Data to hash
   * @return Hex-encoded hash string
   */
  public static String hmacSHA512(String key, String data) {
    return hmac(HMAC_SHA512, key, data);
  }

  /**
   * Generate HMAC SHA256 hash for MoMo.
   *
   * @param key Secret key
   * @param data Data to hash
   * @return Hex-encoded hash string
   */
  public static String hmacSHA256(String key, String data) {
    return hmac(HMAC_SHA256, key, data);
  }

  private static String hmac(String algorithm, String key, String data) {
    try {
      Mac mac = Mac.getInstance(algorithm);
      SecretKeySpec secretKeySpec =
          new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
      mac.init(secretKeySpec);
      byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(hashBytes);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException("Error generating HMAC", e);
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  /**
   * Build query string from params map (sorted alphabetically).
   *
   * @param params Map of parameters
   * @param encode Whether to URL-encode values
   * @return Query string
   */
  public static String buildQueryString(Map<String, String> params, boolean encode) {
    TreeMap<String, String> sortedParams = new TreeMap<>(params);
    return sortedParams.entrySet().stream()
        .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
        .map(
            e -> {
              String key = encode ? urlEncode(e.getKey()) : e.getKey();
              String value = encode ? urlEncode(e.getValue()) : e.getValue();
              return key + "=" + value;
            })
        .collect(Collectors.joining("&"));
  }

  /**
   * URL encode a string.
   *
   * @param value Value to encode
   * @return Encoded string
   */
  public static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  /**
   * Format LocalDateTime to VNPay date format (yyyyMMddHHmmss).
   *
   * @param dateTime DateTime to format
   * @return Formatted string
   */
  public static String formatVnPayDate(LocalDateTime dateTime) {
    return dateTime.format(VN_DATE_FORMAT);
  }

  /**
   * Parse VNPay date string to LocalDateTime.
   *
   * @param dateStr Date string in yyyyMMddHHmmss format
   * @return LocalDateTime
   */
  public static LocalDateTime parseVnPayDate(String dateStr) {
    return LocalDateTime.parse(dateStr, VN_DATE_FORMAT);
  }

  /**
   * Generate unique transaction reference.
   *
   * @param orderId Order ID
   * @return Transaction reference
   */
  public static String generateTxnRef(Long orderId) {
    return orderId + "_" + System.currentTimeMillis();
  }

  /**
   * Extract order ID from transaction reference.
   *
   * @param txnRef Transaction reference
   * @return Order ID
   */
  public static Long extractOrderIdFromTxnRef(String txnRef) {
    if (txnRef == null || !txnRef.contains("_")) {
      return null;
    }
    try {
      return Long.parseLong(txnRef.split("_")[0]);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
