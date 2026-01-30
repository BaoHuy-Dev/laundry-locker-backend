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
  public static final String ROOT_URI_ADMIN_AUTH = "/api/admin/auth";
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

  // ===== Admin Auth Endpoints =====
  public static final String ADMIN_AUTH_LOGIN = "/login";
  public static final String ADMIN_AUTH_VERIFY_2FA = "/verify-2fa";
  public static final String ADMIN_AUTH_REFRESH = "/refresh";

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
  public static final String UPDATE_WEIGHT = "/{orderId}/weight";

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

  // ===== IoT Module URIs =====
  public static final String ROOT_URI_IOT = "/api/iot";
  public static final String IOT_VERIFY_PIN = "/verify-pin";
  public static final String IOT_UNLOCK = "/unlock";
  public static final String IOT_PICKUP = "/pickup";
  public static final String IOT_BOX_STATUS = "/box-status";

  // ===== Order - Customer Endpoints =====
  public static final String MY_ORDERS = "/my-orders";
  public static final String COMPLETE = "/{orderId}/complete";

  // ===== User Endpoints (Extended) =====
  public static final String UPDATE_PROFILE = "/profile";
  public static final String CHANGE_PASSWORD = "/password";
  public static final String FCM_TOKEN = "/fcm-token";

  // ===== Staff Module URIs =====
  public static final String ROOT_URI_STAFF = "/api/staff";
  public static final String STAFF_ORDERS = "/orders";
  public static final String STAFF_ORDERS_WAITING = "/orders/waiting";
  public static final String STAFF_ORDERS_PROCESSING = "/orders/processing";
  public static final String STAFF_ORDERS_READY = "/orders/ready";
  public static final String STAFF_ASSIGN = "/orders/{orderId}/assign";
  public static final String STAFF_MY_ASSIGNED = "/orders/my-assigned";
  public static final String STAFF_LOCKERS = "/lockers";
  public static final String STAFF_UNLOCK_BOX = "/unlock-box";

  // ===== Admin Order Management =====
  public static final String ROOT_URI_ADMIN_ORDERS = "/api/admin/orders";
  public static final String ADMIN_ORDERS_STATISTICS = "/statistics";
  public static final String ROOT_URI_ADMIN_PAYMENTS = "/api/admin/payments";
  public static final String ADMIN_REVENUE = "/revenue";
  public static final String ADMIN_PAYMENT_BY_ID = "/{paymentId}";

  // ===== Admin Scheduler =====
  public static final String ROOT_URI_ADMIN_SCHEDULER = "/api/admin/scheduler";
  public static final String SCHEDULER_AUTO_CANCEL = "/auto-cancel";
  public static final String SCHEDULER_RELEASE_BOXES = "/release-boxes";
  public static final String SCHEDULER_PICKUP_REMINDERS = "/pickup-reminders";
  public static final String SCHEDULER_STATUS = "/status";

  // ===== Loyalty Module URIs =====
  public static final String ROOT_URI_LOYALTY = "/api/loyalty";
  public static final String LOYALTY_SUMMARY = "/summary";
  public static final String LOYALTY_POINTS = "/points";
  public static final String LOYALTY_POINTS_HISTORY = "/points/history";
  public static final String LOYALTY_STAMPS = "/stamps";
  public static final String LOYALTY_STAMPS_BY_ID = "/stamps/{stampCardId}";
  public static final String LOYALTY_REDEEM_POINTS = "/redeem-points";
  public static final String LOYALTY_REDEEM_STAMP = "/redeem-stamp";

  // ===== Admin Loyalty =====
  public static final String ROOT_URI_ADMIN_LOYALTY = "/api/admin/loyalty";
  public static final String ADMIN_LOYALTY_BY_USER = "/users/{userId}";
  public static final String ADMIN_LOYALTY_ADJUST_POINTS = "/users/{userId}/points";
  public static final String ADMIN_LOYALTY_STATISTICS = "/statistics";

  // ===== Partner Module URIs =====
  public static final String ROOT_URI_PARTNER = "/api/partner";
  public static final String PARTNER_DASHBOARD = "/dashboard";
  public static final String PARTNER_STORES = "/stores";
  public static final String PARTNER_STORES_BY_ID = "/stores/{storeId}";
  public static final String PARTNER_STAFF = "/staff";
  public static final String PARTNER_STAFF_BY_ID = "/staff/{staffId}";
  public static final String PARTNER_ORDERS = "/orders";
  public static final String PARTNER_ORDERS_PENDING = "/orders/pending";
  public static final String PARTNER_ORDERS_BY_ID = "/orders/{orderId}";
  public static final String PARTNER_ORDERS_ACCEPT = "/orders/{orderId}/accept";
  public static final String PARTNER_ORDERS_PROCESS = "/orders/{orderId}/process";
  public static final String PARTNER_ORDERS_READY = "/orders/{orderId}/ready";
  public static final String PARTNER_ORDERS_WEIGHT = "/orders/{orderId}/weight";
  public static final String PARTNER_ORDERS_STATISTICS = "/orders/statistics";
  public static final String PARTNER_LOCKERS = "/lockers";
  public static final String PARTNER_LOCKERS_BOXES_AVAILABLE =
      "/lockers/{lockerId}/boxes/available";
  public static final String PARTNER_REVENUE = "/revenue";

  // ===== Staff Access Code URIs =====
  public static final String PARTNER_ACCESS_CODES = "/access-codes";
  public static final String PARTNER_ACCESS_CODES_GENERATE = "/access-codes/generate";
  public static final String PARTNER_ACCESS_CODES_BY_ORDER = "/access-codes/order/{orderId}";
  public static final String PARTNER_ACCESS_CODES_BY_ID = "/access-codes/{codeId}";
  public static final String PARTNER_ACCESS_CODES_CANCEL = "/access-codes/{codeId}/cancel";

  // ===== IoT Staff Code Unlock =====
  public static final String IOT_UNLOCK_WITH_CODE = "/unlock-with-code";

  // ===== Admin Partner Management =====
  public static final String ROOT_URI_ADMIN_PARTNERS = "/api/admin/partners";
  public static final String ADMIN_PARTNERS_BY_ID = "/{partnerId}";
  public static final String ADMIN_PARTNERS_APPROVE = "/{partnerId}/approve";
  public static final String ADMIN_PARTNERS_REJECT = "/{partnerId}/reject";
  public static final String ADMIN_PARTNERS_STORES = "/{partnerId}/stores";
}
