package com.huynqb.laundrylockerbackend.module.store.service;

import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for Store operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

  private final StoreRepository storeRepository;

  @Transactional(readOnly = true)
  public List<StoreResponse> getAllStores() {
    return storeRepository.findByDeleteFlagFalse().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public StoreResponse getStoreById(Long id) {
    Store store =
        storeRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Store not found"));
    return mapToResponse(store);
  }

  private StoreResponse mapToResponse(Store store) {
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
