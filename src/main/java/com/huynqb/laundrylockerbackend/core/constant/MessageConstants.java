package com.huynqb.laundrylockerbackend.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Message constants for i18n keys. All keys should match those defined in messages_*.properties
 * files.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageConstants {

  // ===== Authentication Success =====
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
  public static final String AUTH_OTP_SEND_FAILED = "AUTH_OTP_SEND_FAILED";
  public static final String AUTH_EMAIL_NEW_USER = "AUTH_EMAIL_NEW_USER";
  public static final String AUTH_EMAIL_LOGIN_SUCCESS = "AUTH_EMAIL_LOGIN_SUCCESS";
  public static final String AUTH_EMAIL_REGISTRATION_SUCCESS = "AUTH_EMAIL_REGISTRATION_SUCCESS";
  public static final String AUTH_REGISTRATION_COMPLETE = "AUTH_REGISTRATION_COMPLETE";

  // ===== User =====
  public static final String USER_PROFILE_OK = "USER_PROFILE_OK";

  // ===== Token =====
  public static final String TOKEN_VALID = "TOKEN_VALID";
  public static final String TOKEN_INVALID = "TOKEN_INVALID";

  // ===== Order Success =====
  public static final String ORDER_CREATED = "ORDER_CREATED";
  public static final String ORDER_RETRIEVED = "ORDER_RETRIEVED";
  public static final String ORDERS_RETRIEVED = "ORDERS_RETRIEVED";
  public static final String ORDER_CHECKOUT_SUCCESS = "ORDER_CHECKOUT_SUCCESS";
  public static final String ORDER_COLLECTED = "ORDER_COLLECTED";
  public static final String ORDER_RETURNED = "ORDER_RETURNED";
  public static final String ORDER_CANCELED = "ORDER_CANCELED";
  public static final String ORDER_CONFIRMED = "ORDER_CONFIRMED";
  public static final String ORDER_PROCESSING = "ORDER_PROCESSING";
  public static final String ORDER_READY = "ORDER_READY";

  // ===== Store/Locker/Service Success =====
  public static final String STORES_RETRIEVED = "STORES_RETRIEVED";
  public static final String STORE_RETRIEVED = "STORE_RETRIEVED";
  public static final String LOCKERS_RETRIEVED = "LOCKERS_RETRIEVED";
  public static final String LOCKER_RETRIEVED = "LOCKER_RETRIEVED";
  public static final String BOXES_RETRIEVED = "BOXES_RETRIEVED";
  public static final String SERVICES_RETRIEVED = "SERVICES_RETRIEVED";
  public static final String SERVICE_RETRIEVED = "SERVICE_RETRIEVED";

  // ===== Common Errors =====
  public static final String E_COM001 = "E_COM001";
  public static final String E_COM002 = "E_COM002";
  public static final String E_COM003 = "E_COM003";
  public static final String E_COM004 = "E_COM004";
  public static final String E_COM005 = "E_COM005";

  // ===== Auth Errors =====
  public static final String E_AUTH001 = "E_AUTH001";
  public static final String E_AUTH002 = "E_AUTH002";
  public static final String E_AUTH003 = "E_AUTH003";
  public static final String E_AUTH004 = "E_AUTH004";
  public static final String E_AUTH005 = "E_AUTH005";
  public static final String E_AUTH006 = "E_AUTH006";
  public static final String E_AUTH007 = "E_AUTH007";

  // ===== Phone Errors =====
  public static final String E_PHONE001 = "E_PHONE001";
  public static final String E_PHONE002 = "E_PHONE002";

  // ===== OTP Errors =====
  public static final String E_OTP001 = "E_OTP001";
  public static final String E_OTP002 = "E_OTP002";

  // ===== Validation Errors =====
  public static final String E_VALIDATION001 = "E_VALIDATION001";
  public static final String E_VALIDATION002 = "E_VALIDATION002";

  // ===== Order Errors =====
  public static final String E_ORDER001 = "E_ORDER001";
  public static final String E_ORDER002 = "E_ORDER002";
  public static final String E_ORDER003 = "E_ORDER003";
  public static final String E_ORDER004 = "E_ORDER004";
  public static final String E_ORDER005 = "E_ORDER005";
  public static final String E_ORDER006 = "E_ORDER006";
  public static final String E_ORDER007 = "E_ORDER007";
  public static final String E_ORDER008 = "E_ORDER008";

  // ===== Entity Errors =====
  public static final String E_USER001 = "E_USER001";
  public static final String E_LOCKER001 = "E_LOCKER001";
  public static final String E_BOX001 = "E_BOX001";
  public static final String E_BOX002 = "E_BOX002";
  public static final String E_BOX003 = "E_BOX003";
  public static final String E_SERVICE001 = "E_SERVICE001";

  // ===== HTTP Errors =====
  public static final String E_HTTP_METHOD_NOT_ALLOWED = "E_HTTP_METHOD_NOT_ALLOWED";
  public static final String E_UNSUPPORTED_MEDIA_TYPE = "E_UNSUPPORTED_MEDIA_TYPE";
  public static final String E_MALFORMED_REQUEST = "E_MALFORMED_REQUEST";
  public static final String E_RESOURCE_NOT_FOUND = "E_RESOURCE_NOT_FOUND";
  public static final String E_FILE_TOO_LARGE = "E_FILE_TOO_LARGE";

  // ===== Database Errors =====
  public static final String E_ENTITY_NOT_FOUND = "E_ENTITY_NOT_FOUND";
  public static final String E_DATA_INTEGRITY = "E_DATA_INTEGRITY";
  public static final String E_DATABASE_ERROR = "E_DATABASE_ERROR";

  // ===== State Errors =====
  public static final String E_ILLEGAL_STATE = "E_ILLEGAL_STATE";
  public static final String E_NOT_IMPLEMENTED = "E_NOT_IMPLEMENTED";

  // ===== Rate Limit Errors =====
  public static final String E_RATE_LIMIT = "E_RATE_LIMIT";

  // ===== External Service Errors =====
  public static final String E_EXTERNAL_SERVICE = "E_EXTERNAL_SERVICE";
}
