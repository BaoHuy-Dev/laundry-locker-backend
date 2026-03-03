package com.huynqb.laundrylockerbackend.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TagConstants {
  public static final String ROOT_TAG_AUTH = "AUTH";
  public static final String ROOT_TAG_USERS = "USERS";
  public static final String ROOT_TAG_ADMIN = "ADMIN";
  public static final String ROOT_TAG_STORES = "STORES";
  public static final String ROOT_TAG_LOCKERS = "LOCKERS";
  public static final String ROOT_TAG_SERVICES = "SERVICES";
  public static final String ROOT_TAG_ORDERS = "ORDERS";
  public static final String ROOT_TAG_PAYMENTS = "PAYMENTS";
  public static final String ROOT_TAG_NOTIFICATIONS = "NOTIFICATIONS";

  // ===== Admin Module Tags =====
  public static final String ROOT_TAG_ADMIN_AUTH = "ADMIN - AUTH";
  public static final String ROOT_TAG_ADMIN_DASHBOARD = "ADMIN - DASHBOARD";
  public static final String ROOT_TAG_ADMIN_USERS = "ADMIN - USERS";
  public static final String ROOT_TAG_ADMIN_STORES = "ADMIN - STORES";
  public static final String ROOT_TAG_ADMIN_SERVICES = "ADMIN - SERVICES";
  public static final String ROOT_TAG_ADMIN_LOCKERS = "ADMIN - LOCKERS";
  public static final String ROOT_TAG_ADMIN_ORDERS = "ADMIN - ORDERS";
  public static final String ROOT_TAG_ADMIN_PAYMENTS = "ADMIN - PAYMENTS";
  public static final String ROOT_TAG_ADMIN_NOTIFICATIONS = "ADMIN - NOTIFICATIONS";

  // ===== IoT Module Tags =====
  public static final String ROOT_TAG_IOT = "IOT";

  // ===== Staff Module Tags =====
  public static final String ROOT_TAG_STAFF = "STAFF";

  // ===== Admin Scheduler Tags =====
  public static final String ROOT_TAG_ADMIN_SCHEDULER = "ADMIN - SCHEDULER";

  // ===== Loyalty Module Tags =====
  public static final String ROOT_TAG_LOYALTY = "LOYALTY";
  public static final String ROOT_TAG_ADMIN_LOYALTY = "ADMIN - LOYALTY";

  // ===== Partner Module Tags =====
  public static final String ROOT_TAG_PARTNER = "PARTNER";
  public static final String ROOT_TAG_ADMIN_PARTNERS = "ADMIN - PARTNERS";
}
