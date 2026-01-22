package com.huynqb.laundrylockerbackend.core.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2AuthenticationFailureHandler - Handles OAuth2 authentication failures.
 * Redirects to frontend with error information.
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
  private String frontendRedirectUri;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {

    log.error("OAuth2 authentication failed: {}", exception.getMessage());

    String errorMessage = exception.getMessage() != null
        ? exception.getMessage()
        : "Authentication failed";

    // Encode error message for URL
    String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

    String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
        .queryParam("error", "true")
        .queryParam("message", encodedError)
        .build()
        .toUriString();

    log.debug("Redirecting to: {}", targetUrl);

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}
