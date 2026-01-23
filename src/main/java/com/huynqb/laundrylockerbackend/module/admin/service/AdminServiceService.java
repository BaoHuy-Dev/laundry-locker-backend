package com.huynqb.laundrylockerbackend.module.admin.service;

import com.huynqb.laundrylockerbackend.core.exception.ResourceNotFoundException;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.CreateServiceRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.AdminServiceResponse;
import com.huynqb.laundrylockerbackend.module.admin.mapper.AdminLaundryServiceMapper;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceStatus;
import com.huynqb.laundrylockerbackend.module.laundry.model.LaundryService;
import com.huynqb.laundrylockerbackend.module.laundry.repository.LaundryServiceRepository;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin Service Service - Manages laundry service operations for administrators.
 *
 * <p>Follows SOLID principles:
 *
 * <ul>
 *   <li>SRP: Only handles admin service management logic
 *   <li>OCP: Uses mapper interface for extensibility
 *   <li>DIP: Depends on abstractions (Repository, Mapper interfaces)
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceService {

  private final LaundryServiceRepository serviceRepository;
  private final StoreRepository storeRepository;
  private final AdminLaundryServiceMapper mapper;

  public Page<AdminServiceResponse> getAllServices(Pageable pageable) {
    return serviceRepository.findAll(pageable).map(mapper::toResponse);
  }

  public AdminServiceResponse getServiceById(Long id) {
    return mapper.toResponse(findServiceById(id));
  }

  @Transactional
  public AdminServiceResponse createService(CreateServiceRequest request) {
    LaundryService service = mapper.toEntity(request);
    service.setStatus(ServiceStatus.ACTIVE);

    if (request.getStoreId() != null) {
      Store store = findStoreById(request.getStoreId());
      service.setStore(store);
    }

    service = serviceRepository.save(service);
    log.info("Admin created service: {}", service.getId());
    return mapper.toResponse(service);
  }

  @Transactional
  public AdminServiceResponse updateService(Long id, CreateServiceRequest request) {
    LaundryService service = findServiceById(id);
    mapper.updateEntity(request, service);

    if (request.getStoreId() != null) {
      Store store = findStoreById(request.getStoreId());
      service.setStore(store);
    }

    service = serviceRepository.save(service);
    log.info("Admin updated service: {}", id);
    return mapper.toResponse(service);
  }

  @Transactional
  public AdminServiceResponse updatePrice(Long id, BigDecimal price) {
    LaundryService service = findServiceById(id);
    service.setPrice(price);
    service = serviceRepository.save(service);
    log.info("Admin updated service {} price to {}", id, price);
    return mapper.toResponse(service);
  }

  @Transactional
  public AdminServiceResponse updateStatus(Long id, Boolean active) {
    LaundryService service = findServiceById(id);
    service.setStatus(active ? ServiceStatus.ACTIVE : ServiceStatus.INACTIVE);
    service = serviceRepository.save(service);
    log.info("Admin {} service: {}", active ? "activated" : "deactivated", id);
    return mapper.toResponse(service);
  }

  @Transactional
  public void deleteService(Long id) {
    LaundryService service = findServiceById(id);
    service.setDeleteFlag(true);
    serviceRepository.save(service);
    log.info("Admin deleted service: {}", id);
  }

  // ==================== Private Helper Methods ====================

  private LaundryService findServiceById(Long id) {
    return serviceRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
  }

  private Store findStoreById(Long storeId) {
    return storeRepository
        .findById(storeId)
        .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + storeId));
  }
}
