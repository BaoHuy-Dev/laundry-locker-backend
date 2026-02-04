package com.huynqb.laundrylockerbackend.module.admin.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateUserRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.UpdateUserRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.UpdateUserRolesRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.UpdateUserStatusRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminUserResponse;
import com.huynqb.laundrylockerbackend.module.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Admin User Management. */
@Tag(name = TagConstants.ROOT_TAG_ADMIN_USERS, description = "Admin User Management APIs")
@RestController
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN_USERS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminUserService adminUserService;
  private final ResponseHelper responseHelper;

  /** Get all users with pagination */
  @Operation(
      summary = "Get All Users",
      description = "Retrieve all users with pagination (Admin only)")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getAllUsers(Pageable pageable) {
    Page<AdminUserResponse> users = adminUserService.getAllUsers(pageable);
    return ResponseEntity.ok(responseHelper.success(users, "USERS_RETRIEVED"));
  }

  /** Create a new user (e.g., Staff account) */
  @Operation(
      summary = "Create User",
      description = "Create a new user with specified roles (Admin only)")
  @PostMapping
  public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(
      @Valid @RequestBody CreateUserRequest request) {
    AdminUserResponse user = adminUserService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(user, "USER_CREATED"));
  }

  /** Get user by ID */
  @Operation(summary = "Get User By ID", description = "Retrieve user details by ID (Admin only)")
  @GetMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(@PathVariable Long id) {
    AdminUserResponse user = adminUserService.getUserById(id);
    return ResponseEntity.ok(responseHelper.success(user, "USER_RETRIEVED"));
  }

  /** Update user info */
  @Operation(summary = "Update User", description = "Update user information (Admin only)")
  @PutMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<AdminUserResponse>> updateUser(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
    AdminUserResponse user = adminUserService.updateUser(id, request);
    return ResponseEntity.ok(responseHelper.success(user, "USER_UPDATED"));
  }

  /** Enable/Disable user */
  @Operation(summary = "Update User Status", description = "Enable or disable a user (Admin only)")
  @PutMapping(UriParamConstants.ADMIN_STATUS)
  public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
    AdminUserResponse user = adminUserService.updateUserStatus(id, request.getEnabled());
    return ResponseEntity.ok(responseHelper.success(user, "USER_STATUS_UPDATED"));
  }

  /** Update user roles */
  @Operation(summary = "Update User Roles", description = "Assign roles to a user (Admin only)")
  @PutMapping(UriParamConstants.ADMIN_ROLES)
  public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRoles(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
    AdminUserResponse user = adminUserService.updateUserRoles(id, request);
    return ResponseEntity.ok(responseHelper.success(user, "USER_ROLES_UPDATED"));
  }

  /** Delete user */
  @Operation(summary = "Delete User", description = "Delete a user (Admin only)")
  @DeleteMapping(UriParamConstants.BY_ID)
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    adminUserService.deleteUser(id);
    return ResponseEntity.ok(responseHelper.success("USER_DELETED"));
  }
}
