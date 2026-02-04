package com.huynqb.laundrylockerbackend.module.admin.service;

import com.huynqb.laundrylockerbackend.core.exception.ResourceNotFoundException;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateUserRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.UpdateUserRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.UpdateUserRolesRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminUserResponse;
import com.huynqb.laundrylockerbackend.module.admin.mapper.AdminUserMapper;
import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final PasswordEncoder passwordEncoder;

  /**
   * Create a new user (e.g., Staff account).
   *
   * @param request create user request
   * @return created admin user response
   */
  @Transactional
  public AdminUserResponse createUser(CreateUserRequest request) {
    // Check if email already exists
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new RuntimeException("Email already in use: " + request.getEmail());
    }

    // Check if phone already exists
    if (request.getPhoneNumber() != null
        && userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
      throw new RuntimeException("Phone number already in use: " + request.getPhoneNumber());
    }

    // Build user
    User user =
        User.builder()
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .provider(AuthProvider.LOCAL)
            .enabled(request.getEnabled() != null ? request.getEnabled() : true)
            .emailVerified(true) // Admin-created users are pre-verified
            .build();

    // Set password if provided
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    // Assign roles
    if (request.getRoles() != null && !request.getRoles().isEmpty()) {
      Set<Role> roles = new HashSet<>();
      for (String roleName : request.getRoles()) {
        try {
          RoleName rn = RoleName.valueOf(roleName.toUpperCase());
          Role role =
              roleRepository
                  .findByName(rn)
                  .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
          roles.add(role);
        } catch (IllegalArgumentException e) {
          throw new RuntimeException("Invalid role: " + roleName);
        }
      }
      user.setRoles(roles);
    } else {
      // Default role is USER
      Role userRole =
          roleRepository
              .findByName(RoleName.USER)
              .orElseThrow(() -> new ResourceNotFoundException("Role USER not found"));
      user.setRoles(Set.of(userRole));
    }

    user = userRepository.save(user);
    log.info("Admin created new user: {} with roles: {}", user.getEmail(), request.getRoles());
    return mapper.toResponse(user);
  }

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
