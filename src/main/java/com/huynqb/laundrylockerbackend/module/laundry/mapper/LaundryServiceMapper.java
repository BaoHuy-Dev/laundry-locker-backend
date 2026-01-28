package com.huynqb.laundrylockerbackend.module.laundry.mapper;

import com.huynqb.laundrylockerbackend.module.laundry.dto.response.ServiceResponse;
import com.huynqb.laundrylockerbackend.module.laundry.model.LaundryService;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper for LaundryService entity. */
@Mapper(componentModel = "spring")
public interface LaundryServiceMapper {

  @Mapping(target = "storeId", source = "store.id")
  @Mapping(target = "storeName", source = "store.name")
  ServiceResponse toResponse(LaundryService service);

  List<ServiceResponse> toResponseList(List<LaundryService> services);
}
