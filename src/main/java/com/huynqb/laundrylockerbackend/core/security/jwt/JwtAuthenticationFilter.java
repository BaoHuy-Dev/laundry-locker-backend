package com.huynqb.laundrylockerbackend.core.security.jwt;

import com.huynqb.laundrylockerbackend.core.security.service.CustomUserDetailsService;
import com.huynqb.laundrylockerbackend.module.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter - Intercepts requests and validates JWT tokens Uses Redis via
 * TokenService for blacklist checking (high performance)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider tokenProvider;
  private final CustomUserDetailsService customUserDetailsService;
  private final TokenService tokenService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String jwt = getJwtFromRequest(request);
      log.debug("JWT Filter - Request URI: {}", request.getRequestURI());
      log.debug("JWT Filter - Token present: {}", jwt != null);

      if (StringUtils.hasText(jwt)) {
        String tokenPreview =
            (jwt != null && jwt.length() > 20)
                ? jwt.substring(0, 20) + "..."
                : (jwt != null ? jwt : "");
        log.debug("JWT Filter - Token extracted: {}", tokenPreview);

        // Check if token is blacklisted (Redis lookup - very fast)
        if (tokenService.isAccessTokenBlacklisted(jwt)) {
          log.warn("JWT Filter - Attempted to use blacklisted token");
          filterChain.doFilter(request, response);
          return;
        }
        log.debug("JWT Filter - Token not blacklisted");

        // Validate and set authentication
        if (tokenProvider.validateToken(jwt)) {
          log.debug("JWT Filter - Token is valid");
          String email = tokenProvider.getEmailFromToken(jwt);
          log.debug("JWT Filter - Extracted email: {}", email);

          UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
          log.debug("JWT Filter - User loaded: {}", userDetails.getUsername());
          log.debug("JWT Filter - User authorities: {}", userDetails.getAuthorities());

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authentication);
          log.info("JWT Filter - Successfully set authentication for user: {}", email);
        } else {
          log.warn("JWT Filter - Token validation failed");
        }
      } else {
        log.debug("JWT Filter - No JWT token found in request");
      }
    } catch (Exception ex) {
      log.error("JWT Filter - Could not set user authentication in security context", ex);
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extract JWT token from Authorization header
   *
   * @param request HTTP request
   * @return JWT token or null
   */
  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
