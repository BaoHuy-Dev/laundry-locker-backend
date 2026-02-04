package com.huynqb.laundrylockerbackend.module.auth.service;

import com.huynqb.laundrylockerbackend.module.auth.dto.request.AdminLoginRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;

/** Service interface for admin authentication operations. */
public interface AdminAuthService {

  /**
   * Authenticate admin user with email and password.
   *
   * @param request The admin login request containing email and password
   * @return AuthResponse with access and refresh tokens
   */
  AuthResponse adminLogin(AdminLoginRequest request);
}
