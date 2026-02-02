package com.huynqb.laundrylockerbackend.module.user.enums;

/**
 * Roles trong hệ thống Laundry Locker. Chỉ có 3 roles: - USER: Khách hàng sử dụng dịch vụ - ADMIN:
 * Quản trị viên hệ thống - PARTNER: Chủ cửa hàng giặt ủi (quản lý staff bên ngoài)
 *
 * <p>Note: Staff là actor bên ngoài, được Partner quản lý thông qua StaffAccessCode, không phải
 * role trong hệ thống.
 */
public enum RoleName {
  USER,
  ADMIN,
  PARTNER
}
