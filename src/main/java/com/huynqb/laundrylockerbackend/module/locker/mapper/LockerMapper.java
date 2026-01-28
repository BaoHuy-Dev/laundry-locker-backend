package com.huynqb.laundrylockerbackend.module.locker.mapper;

import com.huynqb.laundrylockerbackend.module.locker.dto.response.BoxResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerResponse;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper for Locker and Box entities. */
@Mapper(componentModel = "spring")
public interface LockerMapper {

  @Mapping(target = "storeId", source = "store.id")
  @Mapping(target = "storeName", source = "store.name")
  @Mapping(target = "totalBoxes", ignore = true) // Set manually in service
  @Mapping(target = "availableBoxes", ignore = true) // Set manually in service
  @Mapping(target = "boxes", ignore = true) // Set manually when needed
  LockerResponse toResponse(Locker locker);

  List<LockerResponse> toResponseList(List<Locker> lockers);

  @Mapping(target = "lockerId", source = "locker.id")
  @Mapping(target = "lockerCode", source = "locker.code")
  BoxResponse toBoxResponse(Box box);

  List<BoxResponse> toBoxResponseList(List<Box> boxes);
}
