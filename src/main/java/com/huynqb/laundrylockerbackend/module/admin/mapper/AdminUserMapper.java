package com.huynqb.laundrylockerbackend.module.admin.mapper;

import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminUserResponse;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for Admin User operations. Follows Single Responsibility Principle - only
 * handles User DTO mapping.
 */
@Mapper(componentModel = "spring")
public interface AdminUserMapper {

  @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
  AdminUserResponse toResponse(User user);

  @Named("rolesToStrings")
  default Set<String> rolesToStrings(Set<Role> roles) {
    if (roles == null) return Collections.emptySet();
    return roles.stream().map(role -> role.getName().name()).collect(Collectors.toSet());
  }
}
