package com.huynqb.laundrylockerbackend.module.user.mapper;

import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** Mapper for User entity. */
@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "createdAt", target = "joinDate")
  @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToStrings")
  UserResponse toResponse(User user);

  @Named("rolesToStrings")
  default Set<String> rolesToStrings(Set<Role> roles) {
    if (roles == null) {
      return null;
    }
    return roles.stream().map(role -> role.getName().name()).collect(Collectors.toSet());
  }
}
