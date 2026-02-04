package com.huynqb.laundrylockerbackend.core.util;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for generating various types of codes. Follows Single Responsibility Principle -
 * only handles code generation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();

  // Characters that are easy to read and distinguish (no 0, O, I, 1, etc.)
  private static final String ALPHANUMERIC_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int DEFAULT_CODE_LENGTH = 8;
  private static final int MAX_GENERATION_ATTEMPTS = 10;

  /**
   * Generate a random alphanumeric code.
   *
   * @param length the length of the code
   * @return generated code
   */
  public static String generateAlphanumericCode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(ALPHANUMERIC_CHARS.charAt(RANDOM.nextInt(ALPHANUMERIC_CHARS.length())));
    }
    return sb.toString();
  }

  /**
   * Generate a random alphanumeric code with default length (8).
   *
   * @return generated code
   */
  public static String generateAlphanumericCode() {
    return generateAlphanumericCode(DEFAULT_CODE_LENGTH);
  }

  /**
   * Generate a unique code with duplicate check.
   *
   * @param length the length of the code
   * @param isDuplicate predicate to check if code already exists
   * @return unique generated code
   */
  public static String generateUniqueCode(int length, Predicate<String> isDuplicate) {
    int attempts = 0;
    String code;
    do {
      code = generateAlphanumericCode(length);
      attempts++;
      if (attempts > MAX_GENERATION_ATTEMPTS) {
        // Fallback to UUID-based code
        code = UUID.randomUUID().toString().replace("-", "").substring(0, length).toUpperCase();
        break;
      }
    } while (isDuplicate.test(code));
    return code;
  }

  /**
   * Generate a unique code with default length.
   *
   * @param isDuplicate predicate to check if code already exists
   * @return unique generated code
   */
  public static String generateUniqueCode(Predicate<String> isDuplicate) {
    return generateUniqueCode(DEFAULT_CODE_LENGTH, isDuplicate);
  }

  /**
   * Generate a numeric PIN code.
   *
   * @param digits number of digits
   * @return generated PIN
   */
  public static String generatePinCode(int digits) {
    if (digits < 1 || digits > 10) {
      throw new IllegalArgumentException("Digits must be between 1 and 10");
    }
    int min = (int) Math.pow(10, digits - 1);
    int max = (int) Math.pow(10, digits) - 1;
    int pin = min + RANDOM.nextInt(max - min + 1);
    return String.valueOf(pin);
  }

  /**
   * Generate a 6-digit PIN code.
   *
   * @return generated 6-digit PIN
   */
  public static String generatePinCode() {
    return generatePinCode(6);
  }

  /**
   * Generate a UUID-based token.
   *
   * @return UUID string
   */
  public static String generateToken() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generate a short token (first 8 characters of UUID).
   *
   * @return short token
   */
  public static String generateShortToken() {
    return UUID.randomUUID().toString().substring(0, 8);
  }
}
