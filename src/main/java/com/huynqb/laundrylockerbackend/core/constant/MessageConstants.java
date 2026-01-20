package com.huynqb.laundrylockerbackend.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Message constants for i18n keys. All keys should match those defined in messages_*.properties
 * files.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageConstants {

  // Authentication Success
  public static final String AUTH_SUCCESS = "AUTH_SUCCESS";
  public static final String AUTH_LOGIN_SUCCESS = "AUTH_LOGIN_SUCCESS";
  public static final String AUTH_REGISTER_SUCCESS = "AUTH_REGISTER_SUCCESS";
  public static final String AUTH_LOGOUT_SUCCESS = "AUTH_LOGOUT_SUCCESS";
  public static final String AUTH_REFRESH_SUCCESS = "AUTH_REFRESH_SUCCESS";
  public static final String AUTH_VERIFICATION_SENT = "AUTH_VERIFICATION_SENT";
  public static final String AUTH_RESET_EMAIL_SENT = "AUTH_RESET_EMAIL_SENT";
  public static final String AUTH_PASSWORD_RESET_SUCCESS = "AUTH_PASSWORD_RESET_SUCCESS";
  public static final String AUTH_EMAIL_VERIFIED = "AUTH_EMAIL_VERIFIED";

  // Phone Authentication
  public static final String AUTH_PHONE_LOGIN_SUCCESS = "AUTH_PHONE_LOGIN_SUCCESS";
  public static final String AUTH_PHONE_NEW_USER = "AUTH_PHONE_NEW_USER";
  public static final String AUTH_PHONE_REGISTRATION_SUCCESS = "AUTH_PHONE_REGISTRATION_SUCCESS";

  // Email OTP Authentication
  public static final String AUTH_OTP_SENT = "AUTH_OTP_SENT";
  public static final String AUTH_OTP_VERIFIED = "AUTH_OTP_VERIFIED";
  public static final String AUTH_EMAIL_NEW_USER = "AUTH_EMAIL_NEW_USER";
  public static final String AUTH_EMAIL_LOGIN_SUCCESS = "AUTH_EMAIL_LOGIN_SUCCESS";
  public static final String AUTH_EMAIL_REGISTRATION_SUCCESS = "AUTH_EMAIL_REGISTRATION_SUCCESS";

  // User
  public static final String USER_PROFILE_OK = "USER_PROFILE_OK";

  // Token
  public static final String TOKEN_VALID = "TOKEN_VALID";
  public static final String TOKEN_INVALID = "TOKEN_INVALID";

  // Common Errors
  public static final String E_COM001 = "E_COM001";
  public static final String E_COM002 = "E_COM002";
  public static final String E_COM003 = "E_COM003";
  public static final String E_COM004 = "E_COM004";
  public static final String E_COM005 = "E_COM005";

  // Auth Errors
  public static final String E_AUTH001 = "E_AUTH001";
  public static final String E_AUTH002 = "E_AUTH002";
  public static final String E_AUTH003 = "E_AUTH003";
  public static final String E_AUTH004 = "E_AUTH004";
  public static final String E_AUTH005 = "E_AUTH005";
  public static final String E_AUTH006 = "E_AUTH006";
  public static final String E_AUTH007 = "E_AUTH007";

  // Phone Errors
  public static final String E_PHONE001 = "E_PHONE001";
  public static final String E_PHONE002 = "E_PHONE002";

  // OTP Errors
  public static final String E_OTP001 = "E_OTP001";
  public static final String E_OTP002 = "E_OTP002";

  // Validation Errors
  public static final String E_VALIDATION001 = "E_VALIDATION001";
  public static final String E_VALIDATION002 = "E_VALIDATION002";
}
