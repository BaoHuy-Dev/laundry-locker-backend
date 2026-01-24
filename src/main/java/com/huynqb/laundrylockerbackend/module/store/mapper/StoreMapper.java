package com.huynqb.laundrylockerbackend.module.store.mapper;

import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import org.springframework.stereotype.Component;

/** Mapper for Store entity. */
@Component
public class StoreMapper {

  public StoreResponse toResponse(Store store) {
    if (store == null) {
      return null;
    }

    return StoreResponse.builder()
        .id(store.getId())
        .name(store.getName())
        .contactPhone(store.getContactPhone())
        .status(store.getStatus())
        .address(store.getAddress())
        .longitude(store.getLongitude())
        .latitude(store.getLatitude())
        .image(store.getImage())
        .description(store.getDescription())
        .createdAt(store.getCreatedAt())
        .updatedAt(store.getUpdatedAt())
        .build();
  }
}
