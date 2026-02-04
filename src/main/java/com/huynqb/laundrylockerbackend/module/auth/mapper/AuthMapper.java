package com.huynqb.laundrylockerbackend.module.auth.mapper;

import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.EmailLoginResponse;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.PhoneLoginResponse;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.mapper.UserMapper;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MapStruct mapper for Auth DTOs. Uses abstract class to allow injection of UserMapper. Provides
 * clean mapping methods for building authentication responses.
 */
@Mapper(componentModel = "spring")
public abstract class AuthMapper {

  @Autowired protected UserMapper userMapper;

  /**
   * Build PhoneLoginResponse for existing user with tokens.
   *
   * @param accessToken JWT access token
   * @param refreshToken Refresh token
   * @param expiresIn Token expiration in seconds
   * @param user User entity to build userInfo
   * @return PhoneLoginResponse with tokens, userInfo and isNewUser=false
   */
  public PhoneLoginResponse toPhoneLoginResponse(
      String accessToken, String refreshToken, long expiresIn, User user) {
    return PhoneLoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(expiresIn)
        .isNewUser(false)
        .userInfo(toUserResponse(user))
        .build();
  }

  /**
   * Build PhoneLoginResponse for new user (no tokens).
   *
   * @param phoneNumber Phone number for registration
   * @param tempToken Temporary token for registration
   * @return PhoneLoginResponse with isNewUser=true
   */
  public PhoneLoginResponse toNewUserPhoneResponse(String phoneNumber, String tempToken) {
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
   * @param user User entity to build userInfo
   * @return EmailLoginResponse with tokens and userInfo
   */
  public EmailLoginResponse toEmailLoginResponse(
      String accessToken, String refreshToken, long expiresIn, User user) {
    return EmailLoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(expiresIn)
        .isNewUser(false)
        .otpVerified(true)
        .userInfo(toUserResponse(user))
        .build();
  }

  /**
   * Build EmailLoginResponse for new user (OTP verified, no tokens).
   *
   * @param tempToken Temporary token for registration
   * @return EmailLoginResponse with isNewUser=true, otpVerified=true
   */
  public EmailLoginResponse toNewUserEmailResponse(String tempToken) {
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
  public AuthResponse toAuthResponse(String accessToken, String refreshToken, long expiresIn) {
    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(expiresIn)
        .build();
  }

  /**
   * Map User entity to UserResponse DTO using UserMapper.
   *
   * @param user User entity
   * @return UserResponse DTO
   */
  public UserResponse toUserResponse(User user) {
    return userMapper.toResponse(user);
  }
}
