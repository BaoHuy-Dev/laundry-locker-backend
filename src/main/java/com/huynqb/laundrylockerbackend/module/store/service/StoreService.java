package com.huynqb.laundrylockerbackend.module.store.service;

import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.store.mapper.StoreMapper;
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
  private final StoreMapper storeMapper;

  @Transactional(readOnly = true)
  public List<StoreResponse> getAllStores() {
    return storeRepository.findByDeleteFlagFalse().stream()
        .map(storeMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public StoreResponse getStoreById(Long id) {
    Store store =
        storeRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Store not found"));
    return storeMapper.toResponse(store);
  }
}
