package com.huynqb.laundrylockerbackend.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageConstants {

  public static final String AUTH_SUCCESS = "AUTH_SUCCESS";
  public static final String LOGOUT_SUCCESS = "LOGOUT_SUCCESS";

  public static final String USER_PROFILE_OK = "USER_PROFILE_OK";

  public static final String E_COM001 = "E_COM001";
  public static final String E_COM002 = "E_COM002";
  public static final String E_COM003 = "E_COM003";
  public static final String E_COM004 = "E_COM004";
  public static final String E_COM005 = "E_COM005";

  // Phone authentication errors
  public static final String E_PHONE001 = "E_PHONE001";
  public static final String E_PHONE002 = "E_PHONE002";

  // Email OTP authentication errors
  public static final String E_AUTH005 = "E_AUTH005"; // User already exists
  public static final String E_AUTH006 = "E_AUTH006"; // Invalid or expired OTP
}
