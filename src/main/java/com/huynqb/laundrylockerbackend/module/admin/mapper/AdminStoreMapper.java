package com.huynqb.laundrylockerbackend.module.admin.mapper;

import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateStoreRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminStoreResponse;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Admin Store operations. Follows Single Responsibility Principle - only
 * handles Store DTO mapping.
 */
@Mapper(componentModel = "spring")
public interface AdminStoreMapper {

  @Mapping(target = "lockerCount", ignore = true) // Set manually in service
  AdminStoreResponse toResponse(Store store);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", ignore = true) // Set manually in service
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deleteFlag", ignore = true)
  Store toEntity(CreateStoreRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deleteFlag", ignore = true)
  void updateEntity(CreateStoreRequest request, @MappingTarget Store store);
}
