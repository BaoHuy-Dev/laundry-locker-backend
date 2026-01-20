package com.huynqb.laundrylockerbackend.module.user.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.i18n.MessageService;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.mapper.UserMapper;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import com.huynqb.laundrylockerbackend.module.user.service.CustomOAuth2User;
import com.huynqb.laundrylockerbackend.module.user.service.CustomOidcUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = TagConstants.ROOT_TAG_USERS)
@RequestMapping(UriParamConstants.ROOT_URI_USERS)
@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;
  private final MessageService messageService;
  private final UserMapper userMapper;

  /** Get user profile - supports both JWT and OAuth2 authentication */
  @GetMapping(UriParamConstants.PROFILE)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
      @AuthenticationPrincipal Object principal) {

    User user = extractUserFromPrincipal(principal);
    UserResponse userResponse = userMapper.toResponse(user);

    return ResponseEntity.ok(
        ApiResponse.<UserResponse>builder()
            .success(true)
            .code("USER_PROFILE_OK")
            .message(messageService.get("USER_PROFILE_OK"))
            .data(userResponse)
            .build());
  }

  @GetMapping(UriParamConstants.DASHBOAR)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> adminDashboard() {
    return ResponseEntity.ok(
        ApiResponse.<String>builder()
            .success(true)
            .data("Admin Dashboard - Access Granted")
            .build());
  }

  @GetMapping(UriParamConstants.READ)
  @PreAuthorize("hasAuthority('READ_PRIVILEGE')")
  public ResponseEntity<ApiResponse<String>> readResource() {
    return ResponseEntity.ok(
        ApiResponse.<String>builder()
            .success(true)
            .data("Reading resource... Access Granted")
            .build());
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
}
