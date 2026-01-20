package com.huynqb.laundrylockerbackend.module.locker.service;

import com.huynqb.laundrylockerbackend.module.locker.dto.response.BoxResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerResponse;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for Locker and Box operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LockerService {

  private final LockerRepository lockerRepository;
  private final BoxRepository boxRepository;

  @Transactional(readOnly = true)
  public List<LockerResponse> getAllLockers() {
    return lockerRepository.findByDeleteFlagFalse().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LockerResponse> getLockersByStore(Long storeId) {
    return lockerRepository.findByStoreId(storeId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public LockerResponse getLockerById(Long id) {
    Locker locker =
        lockerRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Locker not found"));
    return mapToResponseWithBoxes(locker);
  }

  @Transactional(readOnly = true)
  public List<BoxResponse> getBoxesByLocker(Long lockerId) {
    return boxRepository.findByLockerId(lockerId).stream()
        .map(this::mapBoxToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BoxResponse> getAvailableBoxes(Long lockerId) {
    return boxRepository.findByLockerIdAndStatus(lockerId, BoxStatus.AVAILABLE).stream()
        .map(this::mapBoxToResponse)
        .collect(Collectors.toList());
  }

  private LockerResponse mapToResponse(Locker locker) {
    List<Box> boxes = locker.getBoxes();
    int totalBoxes = boxes.size();
    int availableBoxes =
        (int) boxes.stream().filter(b -> b.getStatus() == BoxStatus.AVAILABLE).count();

    return LockerResponse.builder()
        .id(locker.getId())
        .code(locker.getCode())
        .name(locker.getName())
        .image(locker.getImage())
        .status(locker.getStatus())
        .address(locker.getAddress())
        .longitude(locker.getLongitude())
        .latitude(locker.getLatitude())
        .description(locker.getDescription())
        .storeId(locker.getStore().getId())
        .storeName(locker.getStore().getName())
        .totalBoxes(totalBoxes)
        .availableBoxes(availableBoxes)
        .createdAt(locker.getCreatedAt())
        .updatedAt(locker.getUpdatedAt())
        .build();
  }

  private LockerResponse mapToResponseWithBoxes(Locker locker) {
    LockerResponse response = mapToResponse(locker);
    response.setBoxes(
        locker.getBoxes().stream().map(this::mapBoxToResponse).collect(Collectors.toList()));
    return response;
  }

  private BoxResponse mapBoxToResponse(Box box) {
    return BoxResponse.builder()
        .id(box.getId())
        .boxNumber(box.getBoxNumber())
        .isActive(box.getIsActive())
        .status(box.getStatus())
        .description(box.getDescription())
        .lockerId(box.getLocker().getId())
        .lockerCode(box.getLocker().getCode())
        .build();
  }
}
