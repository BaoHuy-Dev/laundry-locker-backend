package com.huynqb.laundrylockerbackend.module.user.mapper;

import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for User entity conversions. Uses Spring component model for dependency
 * injection.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

  /**
   * Convert User entity to UserResponse DTO. Used for API responses that need user profile data.
   */
  UserResponse toResponse(User user);
}
