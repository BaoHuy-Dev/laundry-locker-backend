package com.huynqb.laundrylockerbackend.module.user.mapper;

import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import org.springframework.stereotype.Component;

/** Mapper for User entity. */
@Component
public class UserMapper {

  public UserResponse toResponse(User user) {
    if (user == null) {
      return null;
    }

    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .imageUrl(user.getImageUrl())
        .provider(user.getProvider())
        .emailVerified(user.getEmailVerified())
        .build();
  }
}
