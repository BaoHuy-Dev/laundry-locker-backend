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
  public static final String ROOT_URI_ADMIN_NOTIFICATIONS = "/api/admin/notifications";

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

  // ===== Kiosk Quick Register =====
  public static final String KIOSK_QUICK_REGISTER = "/kiosk/quick-register";

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
  public static final String BY_ORDER_CODE = "/code/{orderCode}";
  public static final String BY_PIN_CODE = "/pin/{pinCode}";
  public static final String ORDER_STATUS = "/{orderId}/status";
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
  public static final String NOTIFICATIONS_READ_BATCH = "/read-batch";
  public static final String NOTIFICATIONS_DELETE = "/{id}";
  public static final String NOTIFICATIONS_DELETE_ALL = "/all";

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
  public static final String ADMIN_IMAGE = "/{id}/image";
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
  public static final String UPDATE_AVATAR = "/avatar";
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
  public static final String PARTNER_ORDERS_COLLECT = "/orders/{orderId}/collect";
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

  // ===== Auth - Password Reset =====
  public static final String FORGOT_PASSWORD = "/forgot-password";
  public static final String RESET_PASSWORD = "/reset-password";

  // ===== User Account Management =====
  public static final String DELETE_ACCOUNT = "/account";
  public static final String CHANGE_PHONE = "/phone";
  public static final String VERIFY_PHONE_CHANGE = "/phone/verify";
  public static final String CHANGE_EMAIL = "/email";
  public static final String VERIFY_EMAIL_CHANGE = "/email/verify";

  // ===== Order - Rating & Timeline =====
  public static final String ORDER_RATE = "/{orderId}/rate";
  public static final String ORDER_RATING = "/{orderId}/rating";
  public static final String ORDER_TIMELINE = "/{orderId}/timeline";
  public static final String ORDER_EXTEND_PICKUP = "/{orderId}/extend-pickup";
  public static final String ORDER_REPROCESS = "/{orderId}/reprocess";
  public static final String ORDER_DUPLICATE = "/{orderId}/duplicate";

  // ===== Payment - Refund & History =====
  public static final String PAYMENT_REFUND = "/{paymentId}/refund";
  public static final String PAYMENT_REFUND_STATUS = "/refund/{refundId}";
  public static final String MY_PAYMENTS = "/my-payments";
  public static final String PAYMENT_RETRY = "/{paymentId}/retry";

  // ===== Store - Nearby & Ratings =====
  public static final String STORE_NEARBY = "/nearby";
  public static final String STORE_RATINGS = "/{storeId}/ratings";
  public static final String STORE_WORKING_HOURS = "/{storeId}/working-hours";

  // ===== Locker - Nearby & Pricing =====
  public static final String LOCKER_NEARBY = "/nearby";
  public static final String LOCKER_PRICING = "/{lockerId}/pricing";

  // ===== Service - Estimation & Popular =====
  public static final String SERVICE_POPULAR = "/popular";
  public static final String SERVICE_ESTIMATE = "/estimate";

  // ===== Partner - Performance & Ratings =====
  public static final String PARTNER_RATINGS = "/ratings";
  public static final String PARTNER_RESPOND_RATING = "/ratings/{ratingId}/respond";
  public static final String PARTNER_PERFORMANCE = "/performance";
  public static final String PARTNER_ORDERS_EXPORT = "/orders/export";
  public static final String PARTNER_NOTIFICATIONS = "/notifications";

  // ===== Staff - Performance & Issues =====
  public static final String STAFF_PERFORMANCE = "/performance";
  public static final String STAFF_REPORT_ISSUE = "/issues";
  public static final String STAFF_ORDER_HISTORY = "/order-history";
  public static final String STAFF_ISSUES_BY_ID = "/issues/{issueId}";

  // ===== IoT - Health & Config =====
  public static final String IOT_HEALTH = "/{deviceId}/health";
  public static final String IOT_CONFIG = "/{deviceId}/config";
  public static final String IOT_ERRORS = "/errors";
  public static final String IOT_DASHBOARD = "/dashboard";

  // ===== Loyalty - Rewards & Expiring =====
  public static final String LOYALTY_REWARDS = "/rewards";
  public static final String LOYALTY_REDEEM_REWARD = "/rewards/{rewardId}/redeem";
  public static final String LOYALTY_EXPIRING_POINTS = "/points/expiring";
  public static final String LOYALTY_TRANSFER_POINTS = "/points/transfer";

  // ===== User - Promotions (public) =====
  public static final String ROOT_URI_PROMOTIONS = "/api/promotions";

  // ===== Admin - System & Promotions =====
  public static final String ROOT_URI_ADMIN_PROMOTIONS = "/api/admin/promotions";
  public static final String ADMIN_SYSTEM_HEALTH = "/system/health";
  public static final String ADMIN_AUDIT_LOGS = "/audit-logs";
  public static final String ADMIN_EXPORT = "/export";
  public static final String ADMIN_BROADCAST = "/broadcast";
  public static final String ADMIN_IOT_DASHBOARD = "/iot/dashboard";
  public static final String ADMIN_PROMOTION_BY_ID = "/{promotionId}";
  public static final String ADMIN_PROMOTION_VALIDATE = "/validate/{code}";

  // ===== Admin Notification Endpoints =====
  public static final String ADMIN_NOTIFICATION_BROADCAST = "/broadcast";
  public static final String ADMIN_NOTIFICATION_SEND = "/send";

  // ===== Additional Order Endpoints =====
  public static final String ORDER_RESET_PIN = "/{orderId}/reset-pin";
  public static final String ORDER_PICKUP_STORAGE = "/{orderId}/pickup-storage";
  public static final String ORDER_COMPLAINT = "/{orderId}/complaint";
  public static final String ORDER_COMPLAINTS = "/{orderId}/complaints";
  public static final String MY_COMPLAINTS = "/my-complaints";
  public static final String MY_RATINGS = "/my-ratings";
  public static final String ORDER_REORDER = "/{orderId}/reorder";

  // ===== Additional Locker Endpoints =====
  public static final String LOCKER_REPORT = "/{id}/report";
  public static final String LOCKER_MY_REPORTS = "/my-reports";

  // ===== Additional Admin Payment Endpoints =====
  public static final String ADMIN_PAYMENT_STATUS = "/{paymentId}/status";

  // ===== Additional Admin Partner Endpoints =====
  public static final String ADMIN_PARTNERS_SUSPEND = "/{partnerId}/suspend";

  // ===== Additional User Endpoints =====
  public static final String USER_STATISTICS = "/me/statistics";

  // ===== Promotion Endpoints =====
  public static final String PROMOTION_ACTIVE = "/active";
  public static final String PROMOTION_VALIDATE_CODE = "/validate/{code}";
  public static final String PROMOTION_STATUS = "/status/{status}";
  public static final String PROMOTION_SEARCH = "/search";

  // ===== Admin Locker Reports Endpoints =====
  public static final String ADMIN_LOCKER_REPORTS = "/reports";
  public static final String ADMIN_LOCKER_REPORTS_RESOLVE = "/reports/{id}/resolve";

  // ===== Additional Payment Endpoints =====
  public static final String PAYMENT_ORDER_REFUNDS = "/order/{orderId}/refunds";

  // ===== Audit Log Endpoints =====
  public static final String AUDIT_LOG_ENTITY = "/entity/{entityType}/{entityId}";
  public static final String AUDIT_LOG_USER = "/user/{userId}";
  public static final String AUDIT_LOG_STATISTICS = "/statistics";
}
