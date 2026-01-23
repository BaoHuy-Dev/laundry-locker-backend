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
  public static final String ROOT_URI_NOTIFICATIONS = "/api/notifications";

  // ===== Admin Module URIs =====
  public static final String ROOT_URI_ADMIN_DASHBOARD = "/api/admin/dashboard";
  public static final String ROOT_URI_ADMIN_USERS = "/api/admin/users";
  public static final String ROOT_URI_ADMIN_STORES = "/api/admin/stores";
  public static final String ROOT_URI_ADMIN_SERVICES = "/api/admin/services";
  public static final String ROOT_URI_ADMIN_LOCKERS = "/api/admin/lockers";

  // ===== Common Path Variables =====
  public static final String HOME = "/";
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

  // ===== Notification Enpoints =====
  public static final String NOTIFICATIONS_BY_ID = "/{id}";
  public static final String NOTIFICATIONS_ALL = "/all";
  public static final String NOTIFICATIONS_UNREAD = "/unread";
  public static final String NOTIFICATIONS_UNREAD_COUNT = "/unread/count";
  public static final String NOTIFICATIONS_READ = "/{id}/read";
  public static final String NOTIFICATIONS_READ_ALL = "/read-all";
  public static final String NOTIFICATIONS_DELETE = "/{id}";

  // ===== Payment Endpoints =====
  public static final String PAYMENT_CREATE = "/create";
  public static final String PAYMENT_BY_ID = "/{paymentId}";
  public static final String PAYMENT_BY_ORDER = "/order/{orderId}";
  public static final String VNPAY_IPN = "/vnpay/ipn";
  public static final String VNPAY_RETURN = "/vnpay/return";
  public static final String MOMO_CALLBACK = "/momo/callback";
  public static final String MOMO_RETURN = "/momo/return";

  // ===== Admin Dashboard Endpoints =====
  public static final String ADMIN_OVERVIEW = "/overview";

  // ===== Admin Common Endpoints =====
  public static final String ADMIN_STATUS = "/{id}/status";
  public static final String ADMIN_ROLES = "/{id}/roles";
  public static final String ADMIN_PRICE = "/{id}/price";
  public static final String ADMIN_MAINTENANCE = "/{id}/maintenance";
  public static final String ADMIN_BOXES = "/{id}/boxes";
  public static final String ADMIN_BOX_STATUS = "/boxes/{boxId}/status";
}
