package com.huynqb.laundrylockerbackend.module.admin.mapper;

import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminLockerResponse;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminLockerResponse.BoxInfo;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/** MapStruct mapper for Admin Locker operations. */
@Mapper(componentModel = "spring")
public interface AdminLockerMapper {

  @Mapping(target = "storeId", source = "store.id")
  @Mapping(target = "storeName", source = "store.name")
  @Mapping(target = "totalBoxes", ignore = true)
  @Mapping(target = "availableBoxes", ignore = true)
  @Mapping(target = "boxes", ignore = true)
  AdminLockerResponse toResponse(Locker locker);

  BoxInfo toBoxInfo(Box box);
}
