package com.huynqb.laundrylockerbackend.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UriParamConstants {

  public static final String ROOT_URI_AUTH = "/api/auth";
  public static final String ROOT_URI_USERS = "/api/user";
  public static final String ROOT_URI_ADMIN = "/api/admin";

  public static final String LOGIN = "/login";
  public static final String REGISTER = "/register";

  public static final String REFRESH_TOKEN = "/refresh-token";
  public static final String LOGOUT = "/logout";

  public static final String VERIFY_EMAIL = "/verify-email";
  public static final String RESEND_VERIFICATION = "resend-verification";
  public static final String FORGET_PASSWORD = "/forget-password";
  public static final String RESET_PASSWORD = "/reset-password";
  public static final String VALIDATE_RESET_TOKEN = "/validate-reset-token";

  public static final String HELLO = "/hello";
  public static final String PROFILE = "/profile";

  public static final String DASHBOAR = "/dashboard";
  public static final String READ = "/read";

  public static final String SECURED = "/secured";
}
