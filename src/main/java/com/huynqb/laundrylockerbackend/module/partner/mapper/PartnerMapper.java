package com.huynqb.laundrylockerbackend.module.partner.mapper;

import com.huynqb.laundrylockerbackend.module.partner.dto.request.PartnerRegistrationRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerResponse;
import com.huynqb.laundrylockerbackend.module.partner.model.Partner;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/** MapStruct mapper for Partner entity. Follows DRY principle by centralizing mapping logic. */
@Mapper(componentModel = "spring")
public interface PartnerMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userName", source = "user", qualifiedByName = "userToName")
  @Mapping(target = "storeCount", source = "stores", qualifiedByName = "setToSize")
  @Mapping(target = "staffCount", source = "staff", qualifiedByName = "setToSize")
  PartnerResponse toResponse(Partner partner);

  List<PartnerResponse> toResponseList(List<Partner> partners);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "approvedAt", ignore = true)
  @Mapping(target = "approvedBy", ignore = true)
  @Mapping(target = "rejectionReason", ignore = true)
  @Mapping(target = "revenueSharePercent", ignore = true)
  @Mapping(target = "stores", ignore = true)
  @Mapping(target = "staff", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deleteFlag", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  Partner toEntity(PartnerRegistrationRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "approvedAt", ignore = true)
  @Mapping(target = "approvedBy", ignore = true)
  @Mapping(target = "rejectionReason", ignore = true)
  @Mapping(target = "revenueSharePercent", ignore = true)
  @Mapping(target = "stores", ignore = true)
  @Mapping(target = "staff", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deleteFlag", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  void updateFromRequest(PartnerRegistrationRequest request, @MappingTarget Partner partner);

  @Named("userToName")
  default String userToName(User user) {
    if (user == null) return null;
    if (user.getFirstName() != null || user.getLastName() != null) {
      String fullName =
          (user.getFirstName() != null ? user.getFirstName() : "")
              + " "
              + (user.getLastName() != null ? user.getLastName() : "");
      return fullName.trim();
    }
    return user.getName();
  }

  @Named("setToSize")
  default int setToSize(Set<?> set) {
    return set != null ? set.size() : 0;
  }
}
