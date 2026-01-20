package com.huynqb.laundrylockerbackend.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UriParamConstants {

  public static final String ROOT_URI_AUTH = "/api/auth";
  public static final String ROOT_URI_USERS = "/api/user";
  public static final String ROOT_URI_ADMIN = "/api/admin";

  // Token Management
  public static final String REFRESH_TOKEN = "/refresh-token";
  public static final String LOGOUT = "/logout";

  // Phone OTP Authentication
  public static final String PHONE_LOGIN = "/phone-login";
  public static final String COMPLETE_REGISTRATION = "/complete-registration";

  // Email OTP Authentication
  public static final String EMAIL_SEND_OTP = "/email/send-otp";
  public static final String EMAIL_VERIFY_OTP = "/email/verify-otp";
  public static final String EMAIL_COMPLETE_REGISTRATION = "/email/complete-registration";

  // User endpoints
  public static final String HELLO = "/hello";
  public static final String PROFILE = "/profile";
  public static final String DASHBOAR = "/dashboard";
  public static final String READ = "/read";
  public static final String SECURED = "/secured";
}
