package com.huynqb.laundrylockerbackend.module.admin.service;

import com.huynqb.laundrylockerbackend.core.exception.ResourceNotFoundException;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.UpdateUserRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.UpdateUserRolesRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminUserResponse;
import com.huynqb.laundrylockerbackend.module.admin.mapper.AdminUserMapper;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin User Service - Manages user operations for administrators.
 *
 * <p>Follows SOLID principles:
 *
 * <ul>
 *   <li>SRP: Only handles admin user management logic
 *   <li>OCP: Uses mapper interface for extensibility
 *   <li>DIP: Depends on abstractions (Repository, Mapper interfaces)
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final AdminUserMapper mapper;

  /**
   * Get all users with pagination.
   *
   * @param pageable pagination parameters
   * @return page of admin user responses
   */
  public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(mapper::toResponse);
  }

  /**
   * Get user by ID.
   *
   * @param id user ID
   * @return admin user response
   * @throws ResourceNotFoundException if user not found
   */
  public AdminUserResponse getUserById(Long id) {
    return mapper.toResponse(findUserById(id));
  }

  /**
   * Update user information.
   *
   * @param id user ID
   * @param request update request
   * @return updated admin user response
   */
  @Transactional
  public AdminUserResponse updateUser(Long id, UpdateUserRequest request) {
    User user = findUserById(id);

    updateIfNotNull(request.getName(), user::setName);
    updateIfNotNull(request.getEmail(), user::setEmail);
    updateIfNotNull(request.getImageUrl(), user::setImageUrl);

    user = userRepository.save(user);
    log.info("Admin updated user: {}", id);
    return mapper.toResponse(user);
  }

  /**
   * Enable or disable user.
   *
   * @param id user ID
   * @param enabled new status
   * @return updated admin user response
   */
  @Transactional
  public AdminUserResponse updateUserStatus(Long id, Boolean enabled) {
    User user = findUserById(id);
    user.setEnabled(enabled);
    user = userRepository.save(user);
    log.info("Admin {} user: {}", enabled ? "enabled" : "disabled", id);
    return mapper.toResponse(user);
  }

  /**
   * Update user roles.
   *
   * @param id user ID
   * @param request roles update request
   * @return updated admin user response
   */
  @Transactional
  public AdminUserResponse updateUserRoles(Long id, UpdateUserRolesRequest request) {
    User user = findUserById(id);
    Set<Role> newRoles = resolveRoles(request.getRoles());
    user.setRoles(newRoles);
    user = userRepository.save(user);
    log.info("Admin updated roles for user {}: {}", id, request.getRoles());
    return mapper.toResponse(user);
  }

  /**
   * Delete user (soft delete is handled by repository).
   *
   * @param id user ID
   */
  @Transactional
  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User not found with id: " + id);
    }
    userRepository.deleteById(id);
    log.info("Admin deleted user: {}", id);
  }

  // ==================== Private Helper Methods ====================

  private User findUserById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
  }

  private Set<Role> resolveRoles(Set<RoleName> roleNames) {
    Set<Role> roles = new HashSet<>();
    for (RoleName roleName : roleNames) {
      Role role =
          roleRepository
              .findByName(roleName)
              .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
      roles.add(role);
    }
    return roles;
  }

  private <T> void updateIfNotNull(T value, java.util.function.Consumer<T> setter) {
    if (value != null) {
      setter.accept(value);
    }
  }
}
