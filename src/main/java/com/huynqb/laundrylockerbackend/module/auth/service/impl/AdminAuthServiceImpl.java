package com.huynqb.laundrylockerbackend.module.auth.service.impl;

import com.huynqb.laundrylockerbackend.core.exception.UnauthorizedException;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.AdminLoginRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.service.AdminAuthService;
import com.huynqb.laundrylockerbackend.module.auth.service.TokenService;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of AdminAuthService for admin authentication. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final TokenService tokenService;

  @Value("${app.security.jwt.refresh-expiration-ms:604800000}")
  private long refreshTokenExpirationMs;

  @Override
  @Transactional(readOnly = true)
  public AuthResponse adminLogin(AdminLoginRequest request) {
    log.info("Admin login attempt for email: {}", request.getEmail());

    // Find user by email
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () -> {
                  log.warn("Admin login failed: User not found for email: {}", request.getEmail());
                  return new UnauthorizedException(
                      "E_AUTH_INVALID_CREDENTIALS", "Invalid email or password");
                });

    // Verify password
    if (user.getPassword() == null
        || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      log.warn("Admin login failed: Invalid password for email: {}", request.getEmail());
      throw new UnauthorizedException("E_AUTH_INVALID_CREDENTIALS", "Invalid email or password");
    }

    // Check if user has ADMIN role
    boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ADMIN);

    if (!isAdmin) {
      log.warn("Admin login failed: User {} does not have ADMIN role", request.getEmail());
      throw new UnauthorizedException(
          "E_AUTH_ACCESS_DENIED", "Access denied. Admin privileges required.");
    }

    // Generate tokens
    String accessToken = jwtTokenProvider.generateTokenFromUser(user);
    String refreshToken = generateRefreshToken();

    // Save refresh token to Redis
    tokenService.saveRefreshToken(refreshToken, user.getId(), refreshTokenExpirationMs);

    log.info("Admin login successful for: {}", request.getEmail());

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtTokenProvider.getRemainingExpirationMs(accessToken) / 1000)
        .build();
  }

  /** Generate a secure random refresh token */
  private String generateRefreshToken() {
    return java.util.UUID.randomUUID().toString();
  }
}
