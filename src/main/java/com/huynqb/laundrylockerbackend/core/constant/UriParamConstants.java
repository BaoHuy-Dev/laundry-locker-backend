package com.huynqb.laundrylockerbackend.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UriParamConstants {

  // ===== Root URIs =====
  public static final String ROOT_URI_AUTH = "/api/auth";
  public static final String ROOT_URI_USERS = "/api/user";
  public static final String ROOT_URI_ADMIN = "/api/admin";
  public static final String ROOT_URI_STORES = "/api/stores";
  public static final String ROOT_URI_LOCKERS = "/api/lockers";
  public static final String ROOT_URI_SERVICES = "/api/services";
  public static final String ROOT_URI_ORDERS = "/api/orders";
  public static final String ROOT_URI_PAYMENTS = "/api/payments";

  // ===== Common Path Variables =====
  public static final String BY_ID = "/{id}";
  public static final String BY_STORE = "/store/{storeId}";

  // ===== Token Management =====
  public static final String REFRESH_TOKEN = "/refresh-token";
  public static final String LOGOUT = "/logout";

  // ===== Phone OTP Authentication =====
  public static final String PHONE_LOGIN = "/phone-login";
  public static final String COMPLETE_REGISTRATION = "/complete-registration";

  // ===== Email OTP Authentication =====
  public static final String EMAIL_SEND_OTP = "/email/send-otp";
  public static final String EMAIL_VERIFY_OTP = "/email/verify-otp";
  public static final String EMAIL_COMPLETE_REGISTRATION = "/email/complete-registration";

  // ===== User Endpoints =====
  public static final String HELLO = "/hello";
  public static final String PROFILE = "/profile";
  public static final String DASHBOAR = "/dashboard";
  public static final String READ = "/read";
  public static final String SECURED = "/secured";

  // ===== Locker Endpoints =====
  public static final String BOXES = "/boxes";
  public static final String BOXES_BY_LOCKER = "/{id}/boxes";
  public static final String BOXES_AVAILABLE = "/{id}/boxes/available";

  // ===== Order Endpoints =====
  public static final String BY_ORDER_ID = "/{orderId}";
  public static final String BY_PIN_CODE = "/pin/{pinCode}";
  public static final String CHECKOUT = "/{orderId}/checkout";
  public static final String COLLECT = "/{orderId}/collect";
  public static final String RETURN = "/{orderId}/return";
  public static final String CANCEL = "/{orderId}/cancel";
  public static final String CONFIRM = "/{orderId}/confirm";
  public static final String PROCESS = "/{orderId}/process";
  public static final String READY = "/{orderId}/ready";
}
