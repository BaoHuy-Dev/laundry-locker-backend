package com.huynqb.laundrylockerbackend.core.security.filter;

import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Email Verification Filter Blocks unverified users from accessing protected APIs. Only allows:
 * login, register, resend-verification, verify-email, forgot-password, reset-password
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationFilter extends OncePerRequestFilter {

  private final UserRepository userRepository;

  // Paths that are allowed for unverified users
  private static final Set<String> ALLOWED_PATHS =
      Set.of(
          "/api/auth/login",
          "/api/auth/register",
          "/api/auth/verify-email",
          "/api/auth/resend-verification",
          "/api/auth/forget-password",
          "/api/auth/forgot-password",
          "/api/auth/reset-password",
          "/api/auth/validate-reset-token",
          "/api/auth/refresh-token",
          "/api/auth/logout",
          "/swagger-ui",
          "/v3/api-docs",
          "/actuator",
          "/error",
          "/oauth2",
          "/login");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    // Skip if path is in allowed list
    if (isPathAllowed(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    // Check authentication
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      // Not authenticated, let Spring Security handle it
      filterChain.doFilter(request, response);
      return;
    }

    // Get user email from authentication
    String email = authentication.getName();

    // Check if user's email is verified
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!Boolean.TRUE.equals(user.getEmailVerified())) {
      log.warn("Unverified user {} attempted to access: {}", email, path);

      // Return 403 with JSON message
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");

      String jsonResponse =
          "{\"success\":false,\"code\":\"E_EMAIL_NOT_VERIFIED\",\"message\":\"Email chưa được xác thực. Vui lòng kiểm tra email và xác thực tài khoản.\"}";

      response.getWriter().write(jsonResponse);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isPathAllowed(String path) {
    return ALLOWED_PATHS.stream().anyMatch(path::startsWith);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Don't filter static resources
    return path.startsWith("/static/") || path.startsWith("/favicon.ico") || path.equals("/");
  }
}
