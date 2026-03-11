package com.huynqb.laundrylockerbackend.module.locker.service;

import com.huynqb.laundrylockerbackend.module.locker.dto.request.LockerReportRequest;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.BoxResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerReportResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerResponse;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.enums.LockerReportStatus;
import com.huynqb.laundrylockerbackend.module.locker.mapper.LockerMapper;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import com.huynqb.laundrylockerbackend.module.locker.model.LockerReport;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerReportRepository;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerRepository;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
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
  private final LockerReportRepository lockerReportRepository;
  private final UserRepository userRepository;
  private final LockerMapper lockerMapper;

  @Transactional(readOnly = true)
  public List<LockerResponse> getAllLockers() {
    return lockerRepository.findByDeleteFlagFalse().stream()
        .map(this::mapToResponseWithCounts)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LockerResponse> getLockersByStore(Long storeId) {
    return lockerRepository.findByStoreId(storeId).stream()
        .map(this::mapToResponseWithCounts)
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
        .map(lockerMapper::toBoxResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BoxResponse> getAvailableBoxes(Long lockerId) {
    return boxRepository.findByLockerIdAndStatus(lockerId, BoxStatus.AVAILABLE).stream()
        .map(lockerMapper::toBoxResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public LockerReportResponse reportLocker(
      Long lockerId, LockerReportRequest request, Long userId) {
    Locker locker =
        lockerRepository
            .findById(lockerId)
            .orElseThrow(() -> new EntityNotFoundException("Locker not found"));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

    LockerReport report =
        LockerReport.builder()
            .locker(locker)
            .user(user)
            .description(request.getDescription())
            .status(LockerReportStatus.PENDING)
            .build();

    report = lockerReportRepository.save(report);
    return LockerReportResponse.from(report);
  }

  /** Get all reports submitted by a specific user. */
  @Transactional(readOnly = true)
  public List<LockerReportResponse> getUserReports(Long userId) {
    return lockerReportRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(LockerReportResponse::from)
        .collect(Collectors.toList());
  }

  /** Map locker to response with box counts. */
  private LockerResponse mapToResponseWithCounts(Locker locker) {
    List<Box> boxes = locker.getBoxes();
    int totalBoxes = boxes.size();
    int availableBoxes =
        (int) boxes.stream().filter(b -> b.getStatus() == BoxStatus.AVAILABLE).count();

    LockerResponse response = lockerMapper.toResponse(locker);
    response.setTotalBoxes(totalBoxes);
    response.setAvailableBoxes(availableBoxes);
    return response;
  }

  /** Map locker to response with box details. */
  private LockerResponse mapToResponseWithBoxes(Locker locker) {
    LockerResponse response = mapToResponseWithCounts(locker);
    response.setBoxes(
        locker.getBoxes().stream().map(lockerMapper::toBoxResponse).collect(Collectors.toList()));
    return response;
  }
}
