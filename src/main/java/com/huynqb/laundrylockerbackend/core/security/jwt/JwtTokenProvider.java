package com.huynqb.laundrylockerbackend.core.security.jwt;

import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/** JWT Token Provider - Handles JWT token generation, parsing, and validation */
@Slf4j
@Component
public class JwtTokenProvider {

  @Value("${app.security.jwt.secret}")
  private String jwtSecret;

  @Value("${app.security.jwt.expiration-ms}")
  private long jwtExpirationMs;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  /**
   * Generate JWT token from Authentication object
   *
   * @param authentication Spring Security authentication
   * @return JWT token string
   */
  public String generateTokenFromAuthentication(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
    return generateToken(
        userPrincipal.getUsername(),
        userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
  }

  /**
   * Generate JWT token from User entity
   *
   * @param user User entity
   * @return JWT token string
   */
  public String generateTokenFromUser(User user) {
    return generateToken(
        user.getEmail(),
        user.getRoles().stream()
            .map(Role::getName)
            .map(roleName -> "ROLE_" + roleName.name())
            .collect(Collectors.toList()));
  }

  /**
   * Core method to generate JWT token
   *
   * @param email User email (used as subject)
   * @param roles List of user roles
   * @return JWT token string
   */
  private String generateToken(String email, java.util.List<String> roles) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

    return Jwts.builder()
        .subject(email)
        .claim("roles", roles)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Extract email (subject) from JWT token
   *
   * @param token JWT token
   * @return User email
   */
  public String getEmailFromToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

    return claims.getSubject();
  }

  /**
   * Validate JWT token
   *
   * @param authToken JWT token to validate
   * @return true if valid, false otherwise
   */
  public boolean validateToken(String authToken) {
    try {
      Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
      return true;
    } catch (MalformedJwtException ex) {
      log.error("Invalid JWT token: {}", ex.getMessage());
    } catch (ExpiredJwtException ex) {
      log.error("Expired JWT token: {}", ex.getMessage());
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported JWT token: {}", ex.getMessage());
    } catch (IllegalArgumentException ex) {
      log.error("JWT claims string is empty: {}", ex.getMessage());
    }
    return false;
  }

  /**
   * Get remaining expiration time in milliseconds Used for Redis TTL when blacklisting tokens
   *
   * @param token JWT token
   * @return Remaining milliseconds until expiration, or 0 if expired/invalid
   */
  public long getRemainingExpirationMs(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
      Date expiration = claims.getExpiration();
      long remainingMs = expiration.getTime() - System.currentTimeMillis();
      return Math.max(0, remainingMs);
    } catch (Exception ex) {
      log.warn("Could not get expiration from token: {}", ex.getMessage());
      return 0;
    }
  }
}
