package com.huynqb.laundrylockerbackend.module.user.service;

import com.huynqb.laundrylockerbackend.module.user.dto.request.ChangePasswordRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.request.FcmTokenRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.request.UpdateProfileRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.mapper.UserMapper;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for user profile management operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  /** Update user profile. */
  @Transactional
  public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
    log.info("Updating profile for user: {}", userId);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    // Update fields if provided
    if (request.getFirstName() != null) {
      user.setFirstName(request.getFirstName());
    }
    if (request.getLastName() != null) {
      user.setLastName(request.getLastName());
    }
    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
      // Check if email is already taken
      if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new RuntimeException("Email already in use");
      }
      user.setEmail(request.getEmail());
      user.setEmailVerified(false); // Need to verify new email
    }
    if (request.getPhoneNumber() != null
        && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
      // Check if phone is already taken
      if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
        throw new RuntimeException("Phone number already in use");
      }
      user.setPhoneNumber(request.getPhoneNumber());
      user.setPhoneVerified(false); // Need to verify new phone
    }
    if (request.getBirthday() != null) {
      user.setBirthday(request.getBirthday());
    }
    if (request.getImageUrl() != null) {
      user.setImageUrl(request.getImageUrl());
    }

    User savedUser = userRepository.save(user);
    log.info("Profile updated for user: {}", userId);

    return userMapper.toResponse(savedUser);
  }

  /** Change user password. */
  @Transactional
  public void changePassword(Long userId, ChangePasswordRequest request) {
    log.info("Changing password for user: {}", userId);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    // Validate new password confirmation
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new RuntimeException("New password and confirm password do not match");
    }

    // Validate current password (only for users with password)
    if (user.getPassword() != null) {
      if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
        throw new RuntimeException("Current password is incorrect");
      }
    }

    // Set new password
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    log.info("Password changed for user: {}", userId);
  }

  /** Register FCM token for push notifications. */
  @Transactional
  public void registerFcmToken(Long userId, FcmTokenRequest request) {
    log.info("Registering FCM token for user: {}", userId);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    // TODO: Store FCM token in a separate table for multi-device support
    // For now, we can store in user or a dedicated FCM token entity
    // This is a placeholder implementation

    log.info("FCM token registered for user: {}", userId);
  }

  /** Remove FCM token (logout from device). */
  @Transactional
  public void removeFcmToken(Long userId, String fcmToken) {
    log.info("Removing FCM token for user: {}", userId);

    // TODO: Remove FCM token from storage
    // This is a placeholder implementation

    log.info("FCM token removed for user: {}", userId);
  }

  /** Get user by ID. */
  @Transactional(readOnly = true)
  public UserResponse getUserById(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    return userMapper.toResponse(user);
  }
}
