package com.huynqb.laundrylockerbackend.core.security.config;

import com.huynqb.laundrylockerbackend.core.security.filter.EmailVerificationFilter;
import com.huynqb.laundrylockerbackend.core.security.handler.CustomAuthenticationEntryPoint;
import com.huynqb.laundrylockerbackend.core.security.handler.OAuth2AuthenticationFailureHandler;
import com.huynqb.laundrylockerbackend.core.security.handler.OAuth2AuthenticationSuccessHandler;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtAuthenticationFilter;
import com.huynqb.laundrylockerbackend.core.security.oauth2.CustomOAuth2UserService;
import com.huynqb.laundrylockerbackend.core.security.oauth2.CustomOidcUserService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Central Spring Security configuration.
 *
 * <p>Defines:
 *
 * <ul>
 *   <li>Multiple ordered {@link SecurityFilterChain}
 *   <li>JWT-based stateless authentication
 *   <li>OAuth2 / OIDC login configuration
 *   <li>Authorization and CORS rules
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  // ==================== Dependencies ====================
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomOidcUserService customOidcUserService;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final EmailVerificationFilter emailVerificationFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  // ==================== Configuration Properties ====================
  @Value("${app.security.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
  private String allowedOrigins;

  @Value("${app.security.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
  private String allowedMethods;

  @Value("${app.security.cors.max-age:3600}")
  private long corsMaxAge;

  // ==================== Public Endpoints ====================
  private static final String[] PUBLIC_ENDPOINTS = {"/", "/error", "/favicon.ico"};

  private static final String[] AUTH_ENDPOINTS = {
    "/api/auth/**", "/api/admin/auth/**", "/oauth2/**", "/login/oauth2/**"
  };

  private static final String[] SWAGGER_ENDPOINTS = {
    "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**"
  };

  private static final String[] ACTUATOR_PUBLIC_ENDPOINTS = {"/actuator/health", "/actuator/info"};

  // Payment gateway callback endpoints (must be public for VNPay/MoMo to send
  // callbacks)
  private static final String[] PAYMENT_CALLBACK_ENDPOINTS = {
    "/api/payments/vnpay/ipn",
    "/api/payments/vnpay/return",
    "/api/payments/momo/callback",
    "/api/payments/momo/return"
  };

  // WebSocket endpoints (must be public for initial connection)
  private static final String[] WEBSOCKET_ENDPOINTS = {"/ws/**", "/ws"};

  // IoT endpoints that use PIN for authentication (no login required)
  private static final String[] IOT_PUBLIC_ENDPOINTS = {
    "/api/iot/verify-pin",
    "/api/iot/unlock",
    "/api/iot/unlock-with-code",
    "/api/iot/box-status",
    "/api/iot/test-mqtt",
    "/api/orders/pin/**"
  };

  // ==================== Security Filter Chains ====================

  /**
   * Actuator security configuration.
   *
   * <p>Health and info are public; other endpoints require ADMIN role.
   */
  @Bean
  @Order(1)
  public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/actuator/**")
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(ACTUATOR_PUBLIC_ENDPOINTS)
                    .permitAll()
                    .anyRequest()
                    .hasRole("ADMIN"))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(csrf -> csrf.disable());

    return http.build();
  }

  /** Main application security configuration. */
  @Bean
  @Order(2)
  public SecurityFilterChain mainSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        // Disable CSRF for stateless APIs
        .csrf(csrf -> csrf.disable())

        // Handle unauthorized attempts - REST APIs should return 401, not redirect to
        // login page
        .exceptionHandling(e -> e.authenticationEntryPoint(customAuthenticationEntryPoint))

        // Apply CORS configuration
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // Use stateless session management
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Configure authorization rules
        .authorizeHttpRequests(this::configureAuthorization)

        // Configure OAuth2 / OIDC login
        .oauth2Login(this::configureOAuth2)

        // Logout configuration
        .logout(logout -> logout.logoutSuccessUrl("/").permitAll())

        // JWT authentication filter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        // Email verification filter
        .addFilterAfter(emailVerificationFilter, JwtAuthenticationFilter.class);

    return http.build();
  }

  // ==================== Configuration Methods ====================

  /** Defines authorization rules for HTTP requests. */
  private void configureAuthorization(
      org.springframework.security.config.annotation.web.configurers
                      .AuthorizeHttpRequestsConfigurer<
                  HttpSecurity>
              .AuthorizationManagerRequestMatcherRegistry
          auth) {
    auth
        // Public endpoints
        .requestMatchers(PUBLIC_ENDPOINTS)
        .permitAll()
        .requestMatchers(AUTH_ENDPOINTS)
        .permitAll()
        .requestMatchers(SWAGGER_ENDPOINTS)
        .permitAll()
        // Payment gateway callbacks (public for VNPay/MoMo)
        .requestMatchers(PAYMENT_CALLBACK_ENDPOINTS)
        .permitAll()
        // WebSocket endpoints (public for initial connection)
        .requestMatchers(WEBSOCKET_ENDPOINTS)
        .permitAll()
        // IoT endpoints that use PIN for authentication
        .requestMatchers(IOT_PUBLIC_ENDPOINTS)
        .permitAll()

        // Role-based access control
        .requestMatchers("/api/admin/**")
        .hasRole("ADMIN")
        .requestMatchers("/api/user/**")
        .hasAnyRole("USER", "ADMIN")

        // Require authentication for all other requests
        .anyRequest()
        .authenticated();
  }

  /** Configures OAuth2 and OIDC login behavior. */
  private void configureOAuth2(
      org.springframework.security.config.annotation.web.configurers.oauth2.client
                  .OAuth2LoginConfigurer<
              HttpSecurity>
          oauth2) {
    oauth2
        .userInfoEndpoint(
            userInfo ->
                userInfo
                    .userService(customOAuth2UserService)
                    .oidcUserService(customOidcUserService))
        .successHandler(oAuth2AuthenticationSuccessHandler)
        .failureHandler(oAuth2AuthenticationFailureHandler);
  }

  // ==================== Beans ====================

  /** Password encoder bean. */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** Authentication manager bean. */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  /** CORS configuration source. */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Allowed origins
    List<String> origins = Arrays.asList(allowedOrigins.split(","));
    configuration.setAllowedOrigins(origins);

    // Allowed HTTP methods
    List<String> methods = Arrays.asList(allowedMethods.split(","));
    configuration.setAllowedMethods(methods);

    // Common CORS settings
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(corsMaxAge);

    // Exposed headers
    configuration.setExposedHeaders(List.of("Authorization", "X-Total-Count"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
