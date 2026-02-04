package com.huynqb.laundrylockerbackend.core.security.handler;

import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.auth.service.TokenService;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import com.huynqb.laundrylockerbackend.module.user.service.CustomOAuth2User;
import com.huynqb.laundrylockerbackend.module.user.service.CustomOidcUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2AuthenticationSuccessHandler - Generates JWT tokens after OAuth2 login. Uses Redis via
 * TokenService for refresh token storage.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final TokenService tokenService;

  @Value("${app.security.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
  private String frontendRedirectUri;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    try {
      User user = extractUserFromAuthentication(authentication);

      if (user == null) {
        log.error("Failed to extract user from authentication");
        handleAuthenticationFailure(request, response, "Failed to extract user information");
        return;
      }

      log.info("OAuth2 Login successful for user: {} (ID: {})", user.getEmail(), user.getId());

      // Generate JWT tokens
      String accessToken = jwtTokenProvider.generateTokenFromUser(user);
      String refreshToken = createRefreshToken(user);

      // Redirect to frontend with tokens
      String targetUrl = buildSuccessRedirectUrl(accessToken, refreshToken);

      if (response.isCommitted()) {
        log.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
        return;
      }

      clearAuthenticationAttributes(request);
      getRedirectStrategy().sendRedirect(request, response, targetUrl);

    } catch (Exception ex) {
      log.error("Error during OAuth2 authentication success handling", ex);
      handleAuthenticationFailure(request, response, "An error occurred during login");
    }
  }

  /**
   * Extract User entity from Authentication principal. Supports both CustomOAuth2User and
   * CustomOidcUser.
   */
  private User extractUserFromAuthentication(Authentication authentication) {
    Object principal = authentication.getPrincipal();

    if (principal instanceof CustomOAuth2User) {
      return ((CustomOAuth2User) principal).getUser();
    } else if (principal instanceof CustomOidcUser) {
      return ((CustomOidcUser) principal).getUser();
    } else if (principal instanceof OAuth2User) {
      // Fallback: lookup by email
      OAuth2User oAuth2User = (OAuth2User) principal;
      String email = oAuth2User.getAttribute("email");
      if (email != null) {
        return userRepository.findByEmail(email).orElse(null);
      }
    }

    return null;
  }

  /** Build redirect URL with tokens as query parameters. */
  private String buildSuccessRedirectUrl(String accessToken, String refreshToken) {
    return UriComponentsBuilder.fromUriString(frontendRedirectUri)
        .queryParam("token", accessToken)
        .queryParam("refreshToken", refreshToken)
        .build()
        .toUriString();
  }

  /** Handle authentication failure by redirecting to frontend with error. */
  private void handleAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, String errorMessage)
      throws IOException {

    String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

    String targetUrl =
        UriComponentsBuilder.fromUriString(frontendRedirectUri)
            .queryParam("error", "true")
            .queryParam("message", encodedError)
            .build()
            .toUriString();

    clearAuthenticationAttributes(request);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  /** Create refresh token and save to Redis. */
  private String createRefreshToken(User user) {
    String tokenValue = UUID.randomUUID().toString();

    // Save to Redis with TTL
    tokenService.saveRefreshToken(tokenValue, user.getId(), refreshExpirationMs);

    log.debug("Refresh token saved to Redis for OAuth2 user: {}", user.getEmail());
    return tokenValue;
  }
}
