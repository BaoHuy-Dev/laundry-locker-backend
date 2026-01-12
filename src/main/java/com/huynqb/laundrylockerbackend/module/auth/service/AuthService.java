package com.huynqb.laundrylockerbackend.module.auth.service;

import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.LoginRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.LogoutRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.RefreshTokenRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.request.RegisterRequest;
import com.huynqb.laundrylockerbackend.module.auth.dto.response.AuthResponse;
import com.huynqb.laundrylockerbackend.module.auth.exception.AuthenticationException;
import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.mapper.UserMapper;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService - Handles authentication business logic Uses Redis via TokenService for
 * high-performance token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final TokenService tokenService;
  private final EmailVerificationService emailVerificationService;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;
  private final UserMapper userMapper;

  @Value("${app.security.jwt.expiration-ms}")
  private long jwtExpirationMs;

  @Value("${app.security.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  /**
   * Authenticate user and generate tokens
   *
   * @param request Login credentials
   * @return Authentication response with tokens
   */
  @Transactional
  public AuthResponse login(LoginRequest request) {
    try {
      // Authenticate with Spring Security
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);

      // Find user
      User user =
          userRepository
              .findByEmail(request.getEmail())
              .orElseThrow(() -> new AuthenticationException("E_AUTH001"));

      // Generate tokens
      String accessToken = jwtTokenProvider.generateTokenFromUser(user);
      String refreshToken = createRefreshToken(user);

      log.info("User logged in successfully: {}", user.getEmail());

      return AuthResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .expiresIn(jwtExpirationMs / 1000)
          .build();

    } catch (org.springframework.security.core.AuthenticationException ex) {
      log.error("Authentication failed for user: {}", request.getEmail());
      throw new AuthenticationException("E_AUTH001");
    }
  }

  /**
   * Register new user with local credentials
   *
   * @param request Registration details
   * @return Authentication response with tokens
   */
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new AuthenticationException("E_AUTH005");
    }

    // Create new user using mapper
    User user = userMapper.toEntity(request);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setProvider(AuthProvider.LOCAL);
    user.setEmailVerified(false);
    user.setRoles(getDefaultRoles());

    User savedUser = userRepository.save(user);
    log.info("New user registered: {}", savedUser.getEmail());

    // Send verification email (async)
    try {
      log.info("Attempting to send verification email to: {}", savedUser.getEmail());
      emailVerificationService.sendVerificationEmail(savedUser);
      log.info("Verification email request sent for: {}", savedUser.getEmail());
    } catch (Exception e) {
      log.error("Failed to send verification email: {}", e.getMessage(), e);
    }

    // Generate tokens
    String accessToken = jwtTokenProvider.generateTokenFromUser(savedUser);
    String refreshToken = createRefreshToken(savedUser);

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtExpirationMs / 1000)
        .build();
  }

  /**
   * Refresh access token using refresh token
   *
   * @param request Refresh token request
   * @return New authentication response
   */
  @Transactional
  public AuthResponse refreshToken(RefreshTokenRequest request) {
    String refreshTokenValue = request.getRefreshToken();

    // Check if refresh token exists in Redis
    Long userId = tokenService.getUserIdByRefreshToken(refreshTokenValue);
    if (userId == null) {
      throw new AuthenticationException("E_AUTH004");
    }

    // Find user
    User user =
        userRepository.findById(userId).orElseThrow(() -> new AuthenticationException("E_AUTH004"));

    // Generate new access token
    String newAccessToken = jwtTokenProvider.generateTokenFromUser(user);

    log.info("Access token refreshed for user: {}", user.getEmail());

    return AuthResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshTokenValue)
        .tokenType("Bearer")
        .expiresIn(jwtExpirationMs / 1000)
        .build();
  }

  /**
   * Logout user by blacklisting access token and deleting refresh token Uses Redis for fast token
   * operations.
   *
   * @param accessToken Current access token
   * @param request Logout request with refresh token
   */
  @Transactional
  public void logout(String accessToken, LogoutRequest request) {
    // Blacklist access token in Redis
    if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
      String email = jwtTokenProvider.getEmailFromToken(accessToken);

      // Calculate remaining TTL for the token
      long remainingMs = jwtTokenProvider.getRemainingExpirationMs(accessToken);
      if (remainingMs > 0) {
        tokenService.blacklistAccessToken(accessToken, remainingMs);
        log.info("Access token blacklisted in Redis for user: {}", email);
      }
    }

    // Delete refresh token from Redis
    String refreshTokenValue = request.getRefreshToken();
    if (refreshTokenValue != null) {
      tokenService.deleteRefreshToken(refreshTokenValue);
      log.info("Refresh token deleted from Redis");
    }
  }

  /**
   * Check if access token is blacklisted
   *
   * @param token Access token
   * @return true if blacklisted
   */
  public boolean isTokenBlacklisted(String token) {
    return tokenService.isAccessTokenBlacklisted(token);
  }

  // ===== Private Helper Methods =====

  /**
   * Create refresh token and save to Redis
   *
   * @param user User entity
   * @return Refresh token string
   */
  private String createRefreshToken(User user) {
    String tokenValue = UUID.randomUUID().toString();

    // Save to Redis with TTL
    tokenService.saveRefreshToken(tokenValue, user.getId(), refreshExpirationMs);

    log.debug("Refresh token saved to Redis for user: {}", user.getEmail());
    return tokenValue;
  }

  /**
   * Get default roles for new users
   *
   * @return Set of default roles
   */
  private Set<Role> getDefaultRoles() {
    Set<Role> roles = new HashSet<>();
    roleRepository.findByName(RoleName.USER).ifPresent(roles::add);
    return roles;
  }
}
