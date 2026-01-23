package com.huynqb.laundrylockerbackend.module.admin.service;

import com.huynqb.laundrylockerbackend.core.exception.ResourceNotFoundException;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateBoxRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateLockerRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminLockerResponse;
import com.huynqb.laundrylockerbackend.module.admin.mapper.AdminLockerMapper;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.enums.LockerStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerRepository;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin Locker Service - Manages locker and box operations for administrators.
 *
 * <p>Follows SOLID principles:
 *
 * <ul>
 *   <li>SRP: Only handles admin locker/box management logic
 *   <li>OCP: Uses mapper interface for extensibility
 *   <li>DIP: Depends on abstractions (Repository, Mapper interfaces)
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLockerService {

  private final LockerRepository lockerRepository;
  private final BoxRepository boxRepository;
  private final StoreRepository storeRepository;
  private final AdminLockerMapper mapper;

  public Page<AdminLockerResponse> getAllLockers(Pageable pageable) {
    return lockerRepository.findAll(pageable).map(this::toResponseWithBoxes);
  }

  public List<AdminLockerResponse> getLockersByStore(Long storeId) {
    return lockerRepository.findByStoreId(storeId).stream()
        .map(this::toResponseWithBoxes)
        .collect(Collectors.toList());
  }

  public AdminLockerResponse getLockerById(Long id) {
    return toResponseWithBoxes(findLockerById(id));
  }

  @Transactional
  public AdminLockerResponse createLocker(CreateLockerRequest request) {
    Store store = findStoreById(request.getStoreId());

    Locker locker =
        Locker.builder()
            .name(request.getName())
            .code(request.getCode())
            .address(request.getAddress())
            .store(store)
            .status(LockerStatus.ACTIVE)
            .build();

    locker = lockerRepository.save(locker);
    log.info("Admin created locker: {}", locker.getId());
    return toResponseWithBoxes(locker);
  }

  @Transactional
  public AdminLockerResponse updateLocker(Long id, CreateLockerRequest request) {
    Locker locker = findLockerById(id);

    updateIfNotNull(request.getName(), locker::setName);
    updateIfNotNull(request.getCode(), locker::setCode);
    updateIfNotNull(request.getAddress(), locker::setAddress);

    if (request.getStoreId() != null) {
      Store store = findStoreById(request.getStoreId());
      locker.setStore(store);
    }

    locker = lockerRepository.save(locker);
    log.info("Admin updated locker: {}", id);
    return toResponseWithBoxes(locker);
  }

  @Transactional
  public AdminLockerResponse setMaintenance(Long id, Boolean maintenance) {
    Locker locker = findLockerById(id);
    locker.setStatus(maintenance ? LockerStatus.MAINTENANCE : LockerStatus.ACTIVE);
    locker = lockerRepository.save(locker);
    log.info("Admin set locker {} maintenance: {}", id, maintenance);
    return toResponseWithBoxes(locker);
  }

  @Transactional
  public AdminLockerResponse addBox(Long lockerId, CreateBoxRequest request) {
    Locker locker = findLockerById(lockerId);

    Box box =
        Box.builder()
            .boxNumber(request.getBoxNumber())
            .description(request.getDescription())
            .status(BoxStatus.AVAILABLE)
            .locker(locker)
            .build();

    boxRepository.save(box);
    log.info("Admin added box {} to locker {}", request.getBoxNumber(), lockerId);
    return toResponseWithBoxes(findLockerById(lockerId));
  }

  @Transactional
  public void updateBoxStatus(Long boxId, BoxStatus status) {
    Box box =
        boxRepository
            .findById(boxId)
            .orElseThrow(() -> new ResourceNotFoundException("Box not found: " + boxId));
    box.setStatus(status);
    boxRepository.save(box);
    log.info("Admin updated box {} status to {}", boxId, status);
  }

  @Transactional
  public void deleteLocker(Long id) {
    Locker locker = findLockerById(id);
    locker.setDeleteFlag(true);
    lockerRepository.save(locker);
    log.info("Admin deleted locker: {}", id);
  }

  // ==================== Private Helper Methods ====================

  private Locker findLockerById(Long id) {
    return lockerRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Locker not found: " + id));
  }

  private Store findStoreById(Long storeId) {
    return storeRepository
        .findById(storeId)
        .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + storeId));
  }

  private AdminLockerResponse toResponseWithBoxes(Locker locker) {
    List<Box> boxes = boxRepository.findByLockerId(locker.getId());
    long availableCount = boxes.stream().filter(b -> b.getStatus() == BoxStatus.AVAILABLE).count();

    AdminLockerResponse response = mapper.toResponse(locker);
    response.setTotalBoxes(boxes.size());
    response.setAvailableBoxes((int) availableCount);
    response.setBoxes(boxes.stream().map(mapper::toBoxInfo).collect(Collectors.toList()));
    return response;
  }

  private <T> void updateIfNotNull(T value, java.util.function.Consumer<T> setter) {
    if (value != null) {
      setter.accept(value);
    }
  }
}
