package com.huynqb.laundrylockerbackend.module.store.mapper;

import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import org.mapstruct.Mapper;


/** Mapper for Store entity. */
@Mapper(componentModel = "spring")
public interface StoreMapper {

  StoreResponse toResponse(Store store);
}
