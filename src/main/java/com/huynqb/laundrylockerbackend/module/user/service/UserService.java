package com.huynqb.laundrylockerbackend.module.user.service;

import com.huynqb.laundrylockerbackend.module.notification.model.FcmToken;
import com.huynqb.laundrylockerbackend.module.notification.repository.FcmTokenRepository;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.user.dto.request.ChangePasswordRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.request.FcmTokenRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.request.UpdateProfileRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserStatisticsResponse;
import com.huynqb.laundrylockerbackend.module.user.mapper.UserMapper;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.Optional;
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
  private final OrderRepository orderRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final FcmTokenRepository fcmTokenRepository;

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

    // Upsert: if token already exists for this user, update it; otherwise create new
    Optional<FcmToken> existingToken = fcmTokenRepository.findByToken(request.getFcmToken());

    if (existingToken.isPresent()) {
      FcmToken fcmToken = existingToken.get();
      // If token belongs to another user, reassign it (device changed user)
      if (!fcmToken.getUser().getId().equals(userId)) {
        fcmToken.setUser(user);
      }
      fcmToken.setDeviceType(request.getDeviceType());
      fcmToken.setDeviceId(request.getDeviceId());
      fcmTokenRepository.save(fcmToken);
    } else {
      FcmToken fcmToken =
          FcmToken.builder()
              .user(user)
              .token(request.getFcmToken())
              .deviceType(request.getDeviceType())
              .deviceId(request.getDeviceId())
              .build();
      fcmTokenRepository.save(fcmToken);
    }

    log.info("FCM token registered for user: {}", userId);
  }

  /** Remove FCM token (logout from device). */
  @Transactional
  public void removeFcmToken(Long userId, String fcmToken) {
    log.info("Removing FCM token for user: {}", userId);

    fcmTokenRepository.deleteByUserIdAndToken(userId, fcmToken);

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

  /** Get user statistics. */
  @Transactional(readOnly = true)
  public UserStatisticsResponse getUserStatistics(Long userId) {
    log.info("Getting statistics for user: {}", userId);
    return UserStatisticsResponse.builder()
        .totalLaundryOrders(orderRepository.countLaundryOrdersByUserId(userId))
        .totalStorageOrders(orderRepository.countStorageOrdersByUserId(userId))
        .totalAmountSpent(orderRepository.sumSpendByUserId(userId))
        .totalVouchersUsed(orderRepository.countVouchersUsedByUserId(userId))
        .build();
  }

  /** Update user avatar. */
  @Transactional
  public UserResponse updateAvatar(Long userId, String imageUrl) {
    log.info("Updating avatar for user: {} with URL: {}", userId, imageUrl);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    user.setImageUrl(imageUrl);
    User updatedUser = userRepository.save(user);

    log.info("Avatar updated successfully for user: {}", userId);
    return userMapper.toResponse(updatedUser);
  }
}
