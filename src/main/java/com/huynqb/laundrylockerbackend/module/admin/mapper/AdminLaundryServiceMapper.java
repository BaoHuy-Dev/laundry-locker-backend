package com.huynqb.laundrylockerbackend.module.admin.mapper;

import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateServiceRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminServiceResponse;
import com.huynqb.laundrylockerbackend.module.laundry.model.LaundryService;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** MapStruct mapper for Admin LaundryService operations. */
@Mapper(componentModel = "spring")
public interface AdminLaundryServiceMapper {

  @Mapping(target = "storeId", source = "store.id")
  @Mapping(
      target = "storeName",
      expression =
          "java(service.getStore() != null ? service.getStore().getName() : \"All Stores\")")
  AdminServiceResponse toResponse(LaundryService service);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "store", ignore = true) // Set manually in service
  @Mapping(target = "status", ignore = true) // Set manually in service
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deleteFlag", ignore = true)
  LaundryService toEntity(CreateServiceRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "store", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deleteFlag", ignore = true)
  void updateEntity(CreateServiceRequest request, @MappingTarget LaundryService service);
}
