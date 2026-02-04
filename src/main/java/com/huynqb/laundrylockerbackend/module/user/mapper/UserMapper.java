package com.huynqb.laundrylockerbackend.module.user.mapper;

import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapper for User entity. */
@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "createdAt", target = "joinDate")
  UserResponse toResponse(User user);
}
