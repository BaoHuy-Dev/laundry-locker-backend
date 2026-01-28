package com.huynqb.laundrylockerbackend.module.auth.mapper;

import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.EmailLoginResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.PhoneLoginResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for Auth DTOs. Provides clean mapping methods for building authentication
 * responses.
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

  /**
   * Build PhoneLoginResponse for existing user with tokens.
   *
   * @param accessToken JWT access token
   * @param refreshToken Refresh token
   * @param expiresIn Token expiration in seconds
   * @return PhoneLoginResponse with tokens and isNewUser=false
   */
  default PhoneLoginResponse toPhoneLoginResponse(
      String accessToken, String refreshToken, long expiresIn) {
    return PhoneLoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(expiresIn)
        .isNewUser(false)
        .build();
  }

  /**
   * Build PhoneLoginResponse for new user (no tokens).
   *
   * @param phoneNumber Phone number for registration
   * @param tempToken Temporary token for registration
   * @return PhoneLoginResponse with isNewUser=true
   */
  default PhoneLoginResponse toNewUserPhoneResponse(String phoneNumber, String tempToken) {
    return PhoneLoginResponse.builder()
        .isNewUser(true)
        .phoneNumber(phoneNumber)
        .tempToken(tempToken)
        .build();
  }

  /**
   * Build EmailLoginResponse for existing user with tokens.
   *
   * @param accessToken JWT access token
   * @param refreshToken Refresh token
   * @param expiresIn Token expiration in seconds
   * @return EmailLoginResponse with tokens
   */
  default EmailLoginResponse toEmailLoginResponse(
      String accessToken, String refreshToken, long expiresIn) {
    return EmailLoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(expiresIn)
        .isNewUser(false)
        .otpVerified(true)
        .build();
  }

  /**
   * Build EmailLoginResponse for new user (OTP verified, no tokens).
   *
   * @param tempToken Temporary token for registration
   * @return EmailLoginResponse with isNewUser=true, otpVerified=true
   */
  default EmailLoginResponse toNewUserEmailResponse(String tempToken) {
    return EmailLoginResponse.builder()
        .isNewUser(true)
        .otpVerified(true)
        .tempToken(tempToken)
        .build();
  }

  /**
   * Build AuthResponse with tokens.
   *
   * @param accessToken JWT access token
   * @param refreshToken Refresh token
   * @param expiresIn Token expiration in seconds
   * @return AuthResponse with tokens
   */
  default AuthResponse toAuthResponse(String accessToken, String refreshToken, long expiresIn) {
    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(expiresIn)
        .build();
  }
}
