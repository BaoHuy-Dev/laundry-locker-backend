package com.huynqb.laundrylockerbackend.module.laundry.service;

import com.huynqb.laundrylockerbackend.module.laundry.dto.response.ServiceResponse;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceCategory;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceStatus;
import com.huynqb.laundrylockerbackend.module.laundry.mapper.LaundryServiceMapper;
import com.huynqb.laundrylockerbackend.module.laundry.model.LaundryService;
import com.huynqb.laundrylockerbackend.module.laundry.repository.LaundryServiceRepository;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for LaundryService operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LaundryServiceService {

  private final LaundryServiceRepository laundryServiceRepository;
  private final LaundryServiceMapper laundryServiceMapper;
  private final LockerRepository lockerRepository;

  @Transactional(readOnly = true)
  public List<ServiceResponse> getAllServices() {
    return laundryServiceRepository.findByDeleteFlagFalse().stream()
        .map(laundryServiceMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ServiceResponse> getServicesByStore(Long storeId) {
    return laundryServiceRepository.findByStoreIdAndStatus(storeId, ServiceStatus.ACTIVE).stream()
        .map(laundryServiceMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ServiceResponse getServiceById(Long id) {
    LaundryService service =
        laundryServiceRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Service not found"));
    return laundryServiceMapper.toResponse(service);
  }

  /**
   * Get services by category (STORAGE or LAUNDRY).
   *
   * @param category the service category
   * @return list of active services in the category
   */
  @Transactional(readOnly = true)
  public List<ServiceResponse> getServicesByCategory(ServiceCategory category) {
    return laundryServiceRepository
        .findByCategoryAndStatusAndDeleteFlagFalse(category, ServiceStatus.ACTIVE)
        .stream()
        .map(laundryServiceMapper::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get services by store and category.
   *
   * @param storeId the store ID
   * @param category the service category
   * @return list of active services
   */
  @Transactional(readOnly = true)
  public List<ServiceResponse> getServicesByStoreAndCategory(
      Long storeId, ServiceCategory category) {
    return laundryServiceRepository
        .findByStoreIdAndCategoryAndStatusAndDeleteFlagFalse(
            storeId, category, ServiceStatus.ACTIVE)
        .stream()
        .map(laundryServiceMapper::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get services by locker ID. This method finds the store associated with the locker and returns
   * services available at that store. Designed for Kiosk app which only knows the Locker ID.
   *
   * @param lockerId the locker ID
   * @return list of active services at the locker's store
   */
  @Transactional(readOnly = true)
  public List<ServiceResponse> getServicesByLocker(Long lockerId) {
    Locker locker =
        lockerRepository
            .findById(lockerId)
            .orElseThrow(
                () -> new EntityNotFoundException("Locker not found with ID: " + lockerId));

    Long storeId = locker.getStore().getId();
    return getServicesByStore(storeId);
  }

  /**
   * Get services by locker ID and category. This method finds the store associated with the locker
   * and returns services filtered by category. Designed for Kiosk app to get STORAGE or LAUNDRY
   * services.
   *
   * @param lockerId the locker ID
   * @param category the service category (STORAGE or LAUNDRY)
   * @return list of active services at the locker's store filtered by category
   */
  @Transactional(readOnly = true)
  public List<ServiceResponse> getServicesByLockerAndCategory(
      Long lockerId, ServiceCategory category) {
    Locker locker =
        lockerRepository
            .findById(lockerId)
            .orElseThrow(
                () -> new EntityNotFoundException("Locker not found with ID: " + lockerId));

    Long storeId = locker.getStore().getId();
    return getServicesByStoreAndCategory(storeId, category);
  }
}
