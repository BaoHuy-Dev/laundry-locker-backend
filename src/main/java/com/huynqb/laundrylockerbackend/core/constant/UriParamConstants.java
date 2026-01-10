package com.huynqb.laundrylockerbackend.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UriParamConstants {

  public static final String ROOT_URI_AUTH = "/api/auth";
  public static final String ROOT_URI_USERS = "/api/user";
  public static final String ROOT_URI_ADMIN = "/api/admin";

  public static final String LOGIN = "/login";
  public static final String REFRESH = "/refresh";
  public static final String LOGOUT = "/logout";

  public static final String HELLO = "/hello";
  public static final String PROFILE = "/profile";
}
