package com.huynqb.laundrylockerbackend.module.user.mapper;

import com.huynqb.laundrylockerbackend.module.auth.dto.request.RegisterRequest;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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

  /**
   * Convert RegisterRequest to User entity. Note: password, roles, provider need to be set manually
   * after mapping.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "provider", ignore = true)
  @Mapping(target = "providerId", ignore = true)
  @Mapping(target = "emailVerified", ignore = true)
  @Mapping(target = "imageUrl", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  User toEntity(RegisterRequest request);

  /** Update existing User entity from RegisterRequest. Useful for profile updates. */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "provider", ignore = true)
  @Mapping(target = "providerId", ignore = true)
  @Mapping(target = "emailVerified", ignore = true)
  @Mapping(target = "imageUrl", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateFromRequest(RegisterRequest request, @MappingTarget User user);
}
