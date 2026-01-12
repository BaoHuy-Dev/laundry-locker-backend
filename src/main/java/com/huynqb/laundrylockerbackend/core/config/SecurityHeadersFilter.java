package com.huynqb.laundrylockerbackend.core.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security Headers Filter Adds security headers to all responses to protect against common attacks:
 * - XSS (Cross-Site Scripting) - Clickjacking - MIME type sniffing - Content Security Policy
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Prevent XSS attacks
    response.setHeader("X-XSS-Protection", "1; mode=block");

    // Prevent MIME type sniffing
    response.setHeader("X-Content-Type-Options", "nosniff");

    // Prevent Clickjacking
    response.setHeader("X-Frame-Options", "DENY");

    // Strict Transport Security (HTTPS only) - 1 year
    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

    // Referrer Policy
    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

    // Permissions Policy (formerly Feature-Policy)
    response.setHeader(
        "Permissions-Policy", "geolocation=(), microphone=(), camera=(), payment=()");

    // Content Security Policy (basic - adjust as needed)
    response.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; "
            + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
            + "style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data: https:; "
            + "font-src 'self' data:; "
            + "connect-src 'self'; "
            + "frame-ancestors 'none';");

    // Cache Control for API responses
    if (request.getRequestURI().startsWith("/api/")) {
      response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
      response.setHeader("Pragma", "no-cache");
    }

    filterChain.doFilter(request, response);
  }
}
