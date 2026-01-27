package com.huynqb.laundrylockerbackend.module.user.mapper;

import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import org.mapstruct.Mapper;


/** Mapper for User entity. */
@Mapper(componentModel = "spring")
public interface UserMapper {

  UserResponse toResponse(User user);
}
