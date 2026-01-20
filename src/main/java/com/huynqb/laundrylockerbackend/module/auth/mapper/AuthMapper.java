package com.huynqb.laundrylockerbackend.module.auth.mapper;

import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.EmailLoginResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.PhoneLoginResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
   * @return PhoneLoginResponse with tokens and isNewUser=false
   */
  @Mapping(target = "isNewUser", constant = "false")
  PhoneLoginResponse toPhoneLoginResponse(String accessToken, String refreshToken);

  /**
   * Build PhoneLoginResponse for new user (no tokens).
   *
   * @return PhoneLoginResponse with isNewUser=true
   */
  default PhoneLoginResponse toNewUserPhoneResponse() {
    return PhoneLoginResponse.builder().isNewUser(true).build();
  }

  /**
   * Build EmailLoginResponse for existing user with tokens.
   *
   * @param accessToken JWT access token
   * @param refreshToken Refresh token
   * @return EmailLoginResponse with tokens
   */
  @Mapping(target = "isNewUser", constant = "false")
  @Mapping(target = "otpVerified", constant = "true")
  EmailLoginResponse toEmailLoginResponse(String accessToken, String refreshToken);

  /**
   * Build EmailLoginResponse for new user (OTP verified, no tokens).
   *
   * @return EmailLoginResponse with isNewUser=true, otpVerified=true
   */
  default EmailLoginResponse toNewUserEmailResponse() {
    return EmailLoginResponse.builder().isNewUser(true).otpVerified(true).build();
  }

  /**
   * Build AuthResponse with tokens.
   *
   * @param accessToken JWT access token
   * @param refreshToken Refresh token
   * @return AuthResponse with tokens
   */
  AuthResponse toAuthResponse(String accessToken, String refreshToken);
}
