package com.huynqb.laundrylockerbackend.module.admin.service;

import com.huynqb.laundrylockerbackend.module.admin.dto.response.DashboardOverviewResponse;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceStatus;
import com.huynqb.laundrylockerbackend.module.laundry.repository.LaundryServiceRepository;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerRepository;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

  private final UserRepository userRepository;
  private final StoreRepository storeRepository;
  private final LockerRepository lockerRepository;
  private final BoxRepository boxRepository;
  private final LaundryServiceRepository serviceRepository;

  public DashboardOverviewResponse getOverview() {
    long totalUsers = userRepository.count();
    long totalStores = storeRepository.count();
    long totalLockers = lockerRepository.count();
    long activeServices = serviceRepository.findByStatus(ServiceStatus.ACTIVE).size();

    // Count boxes by status
    long totalBoxes = boxRepository.count();
    long availableBoxes = 0;
    long occupiedBoxes = 0;

    try {
      // This is a simplified version - in production, use proper queries
      availableBoxes =
          boxRepository.findAll().stream()
              .filter(b -> b.getStatus() == BoxStatus.AVAILABLE)
              .count();
      occupiedBoxes =
          boxRepository.findAll().stream().filter(b -> b.getStatus() == BoxStatus.OCCUPIED).count();
    } catch (Exception e) {
      log.warn("Error counting boxes: {}", e.getMessage());
    }

    return DashboardOverviewResponse.builder()
        .totalUsers(totalUsers)
        .totalStores(totalStores)
        .totalLockers(totalLockers)
        .totalOrders(0L) // Order module not implemented yet
        .ordersToday(0L)
        .pendingOrders(0L)
        .totalRevenue(BigDecimal.ZERO)
        .revenueToday(BigDecimal.ZERO)
        .activeServices(activeServices)
        .availableBoxes(availableBoxes)
        .occupiedBoxes(occupiedBoxes)
        .build();
  }
}
