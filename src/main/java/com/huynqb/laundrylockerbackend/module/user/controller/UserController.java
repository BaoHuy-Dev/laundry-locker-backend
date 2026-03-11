package com.huynqb.laundrylockerbackend.module.user.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.dto.UpdateImageRequest;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.user.dto.request.ChangePasswordRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.request.FcmTokenRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.request.UpdateProfileRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserStatisticsResponse;
import com.huynqb.laundrylockerbackend.module.user.mapper.UserMapper;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import com.huynqb.laundrylockerbackend.module.user.service.CustomOAuth2User;
import com.huynqb.laundrylockerbackend.module.user.service.CustomOidcUser;
import com.huynqb.laundrylockerbackend.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for User operations. */
@Tag(name = TagConstants.ROOT_TAG_USERS)
@RequestMapping(UriParamConstants.ROOT_URI_USERS)
@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;
  private final ResponseHelper responseHelper;
  private final UserMapper userMapper;
  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  /** Get user profile - supports both JWT and OAuth2 authentication */
  @Operation(
      summary = "Get User Profile",
      description = "Retrieve current user's profile information")
  @GetMapping(UriParamConstants.PROFILE)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
      @AuthenticationPrincipal Object principal) {

    User user = extractUserFromPrincipal(principal);
    UserResponse userResponse = userMapper.toResponse(user);

    return ResponseEntity.ok(responseHelper.success(userResponse, "USER_PROFILE_OK"));
  }

  @Operation(
      summary = "Get User Statistics",
      description = "Retrieve current user's order and spending statistics")
  @GetMapping(UriParamConstants.USER_STATISTICS)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserStatisticsResponse>> getUserStatistics(
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    UserStatisticsResponse response = userService.getUserStatistics(userId);
    return ResponseEntity.ok(responseHelper.success(response, "USER_STATISTICS_RETRIEVED"));
  }

  @Operation(summary = "Admin Dashboard", description = "Access admin dashboard (Admin only)")
  @GetMapping(UriParamConstants.DASHBOAR)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> adminDashboard() {
    return ResponseEntity.ok(
        ApiResponse.<String>builder()
            .success(true)
            .data("Admin Dashboard - Access Granted")
            .build());
  }

  @Operation(summary = "Read Resource", description = "Read resource with READ_PRIVILEGE")
  @GetMapping(UriParamConstants.READ)
  @PreAuthorize("hasAuthority('READ_PRIVILEGE')")
  public ResponseEntity<ApiResponse<String>> readResource() {
    return ResponseEntity.ok(
        ApiResponse.<String>builder()
            .success(true)
            .data("Reading resource... Access Granted")
            .build());
  }

  /** Update user profile. */
  @Operation(summary = "Update Profile", description = "Update current user's profile information")
  @PutMapping(UriParamConstants.UPDATE_PROFILE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
      @Valid @RequestBody UpdateProfileRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    UserResponse response = userService.updateProfile(userId, request);
    return ResponseEntity.ok(responseHelper.success(response, "PROFILE_UPDATED"));
  }

  /** Update user avatar. */
  @Operation(summary = "Update Avatar", description = "Update current user's avatar image")
  @PutMapping(UriParamConstants.UPDATE_AVATAR)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
      @Valid @RequestBody UpdateImageRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    UserResponse response = userService.updateAvatar(userId, request.getImageUrl());
    return ResponseEntity.ok(responseHelper.success(response, "AVATAR_UPDATED"));
  }

  /** Change password. */
  @Operation(summary = "Change Password", description = "Change current user's password")
  @PutMapping(UriParamConstants.CHANGE_PASSWORD)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    userService.changePassword(userId, request);
    return ResponseEntity.ok(responseHelper.success("PASSWORD_CHANGED"));
  }

  /** Register FCM token for push notifications. */
  @Operation(
      summary = "Register FCM Token",
      description = "Register FCM token for push notifications")
  @PostMapping(UriParamConstants.FCM_TOKEN)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> registerFcmToken(
      @Valid @RequestBody FcmTokenRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    userService.registerFcmToken(userId, request);
    return ResponseEntity.ok(responseHelper.success("FCM_TOKEN_REGISTERED"));
  }

  /** Remove FCM token. */
  @Operation(summary = "Remove FCM Token", description = "Remove FCM token (logout from device)")
  @DeleteMapping(UriParamConstants.FCM_TOKEN)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> removeFcmToken(
      @RequestParam String fcmToken, @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    userService.removeFcmToken(userId, fcmToken);
    return ResponseEntity.ok(responseHelper.success("FCM_TOKEN_REMOVED"));
  }

  /**
   * Extract user from different principal types Supports: UserDetails (JWT), CustomOAuth2User,
   * CustomOidcUser
   */
  private User extractUserFromPrincipal(Object principal) {
    if (principal instanceof UserDetails) {
      // JWT Authentication - principal is UserDetails
      // Username can be email OR phone number
      String identifier = ((UserDetails) principal).getUsername();
      return userRepository
          .findByEmail(identifier)
          .or(() -> userRepository.findByPhoneNumber(identifier))
          .orElseThrow(() -> new RuntimeException("User not found: " + identifier));
    } else if (principal instanceof CustomOidcUser) {
      // OAuth2 OIDC (Google)
      return ((CustomOidcUser) principal).getUser();
    } else if (principal instanceof CustomOAuth2User) {
      // OAuth2 (GitHub, Facebook, Zalo)
      return ((CustomOAuth2User) principal).getUser();
    }
    throw new IllegalStateException("Unknown principal type: " + principal.getClass().getName());
  }

  private Long extractUserId(String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return jwtTokenProvider.getUserIdFromToken(token);
  }
}
