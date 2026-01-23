package com.huynqb.laundrylockerbackend.module.admin.service;

import com.huynqb.laundrylockerbackend.core.exception.ResourceNotFoundException;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateStoreRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminStoreResponse;
import com.huynqb.laundrylockerbackend.module.admin.mapper.AdminStoreMapper;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerRepository;
import com.huynqb.laundrylockerbackend.module.store.enums.StoreStatus;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin Store Service - Manages store operations for administrators.
 *
 * <p>Follows SOLID principles:
 *
 * <ul>
 *   <li>SRP: Only handles admin store management logic
 *   <li>OCP: Uses mapper interface for extensibility
 *   <li>DIP: Depends on abstractions (Repository, Mapper interfaces)
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStoreService {

  private final StoreRepository storeRepository;
  private final LockerRepository lockerRepository;
  private final AdminStoreMapper mapper;

  public Page<AdminStoreResponse> getAllStores(Pageable pageable) {
    return storeRepository.findAll(pageable).map(this::toResponseWithLockerCount);
  }

  public AdminStoreResponse getStoreById(Long id) {
    return toResponseWithLockerCount(findStoreById(id));
  }

  @Transactional
  public AdminStoreResponse createStore(CreateStoreRequest request) {
    Store store = mapper.toEntity(request);
    store.setStatus(StoreStatus.ACTIVE);
    store = storeRepository.save(store);
    log.info("Admin created store: {}", store.getId());
    return toResponseWithLockerCount(store);
  }

  @Transactional
  public AdminStoreResponse updateStore(Long id, CreateStoreRequest request) {
    Store store = findStoreById(id);
    mapper.updateEntity(request, store);
    store = storeRepository.save(store);
    log.info("Admin updated store: {}", id);
    return toResponseWithLockerCount(store);
  }

  @Transactional
  public AdminStoreResponse updateStoreStatus(Long id, Boolean active) {
    Store store = findStoreById(id);
    store.setStatus(active ? StoreStatus.ACTIVE : StoreStatus.INACTIVE);
    store = storeRepository.save(store);
    log.info("Admin {} store: {}", active ? "activated" : "deactivated", id);
    return toResponseWithLockerCount(store);
  }

  @Transactional
  public void deleteStore(Long id) {
    Store store = findStoreById(id);
    store.setDeleteFlag(true);
    storeRepository.save(store);
    log.info("Admin deleted store: {}", id);
  }

  // ==================== Private Helper Methods ====================

  private Store findStoreById(Long id) {
    return storeRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + id));
  }

  private AdminStoreResponse toResponseWithLockerCount(Store store) {
    AdminStoreResponse response = mapper.toResponse(store);
    int lockerCount = lockerRepository.findByStoreId(store.getId()).size();
    response.setLockerCount(lockerCount);
    return response;
  }
}
