package com.huynqb.laundrylockerbackend.core.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * FirebaseService - Handles Firebase authentication operations. Single Responsibility: Only deals
 * with Firebase token verification.
 */
@Slf4j
@Service
public class FirebaseService {

  /**
   * Verify Firebase ID token and return decoded token.
   *
   * @param idToken Firebase ID token from client
   * @return Decoded FirebaseToken containing user info
   * @throws com.huynqb.laundrylockerbackend.module.auth.exception.FirebaseAuthException if
   *     verification fails
   */
  public FirebaseToken verifyIdToken(String idToken) {
    try {
      FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
      log.debug("Firebase token verified for uid: {}", decodedToken.getUid());
      return decodedToken;
    } catch (FirebaseAuthException e) {
      log.error("Firebase token verification failed: {}", e.getMessage());
      throw new com.huynqb.laundrylockerbackend.module.auth.exception.FirebaseAuthException(
          "E_PHONE001", "Invalid Firebase token");
    }
  }

  /**
   * Extract phone number from Firebase token.
   *
   * @param token Decoded Firebase token
   * @return Phone number string
   * @throws com.huynqb.laundrylockerbackend.module.auth.exception.FirebaseAuthException if phone
   *     not found
   */
  public String extractPhoneNumber(FirebaseToken token) {
    String phoneNumber =
        token.getClaims().get("phone_number") != null
            ? token.getClaims().get("phone_number").toString()
            : null;

    if (phoneNumber == null || phoneNumber.isBlank()) {
      log.error("Phone number not found in Firebase token for uid: {}", token.getUid());
      throw new com.huynqb.laundrylockerbackend.module.auth.exception.FirebaseAuthException(
          "E_PHONE002", "Phone number not found in token");
    }

    return phoneNumber;
  }
}
