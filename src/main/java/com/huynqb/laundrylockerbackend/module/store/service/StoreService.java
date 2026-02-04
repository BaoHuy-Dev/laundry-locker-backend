package com.huynqb.laundrylockerbackend.module.store.service;

import com.huynqb.laundrylockerbackend.module.store.dto.request.NearbyStoreRequest;
import com.huynqb.laundrylockerbackend.module.store.dto.response.NearbyStoreResponse;
import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.store.mapper.StoreMapper;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
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

  // Earth's radius in meters
  private static final double EARTH_RADIUS_METERS = 6371000;

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

  /** Find nearby stores based on location. */
  @Transactional(readOnly = true)
  public List<NearbyStoreResponse> findNearbyStores(NearbyStoreRequest request) {
    log.info(
        "Finding nearby stores: lat={}, lon={}, radius={}",
        request.getLatitude(),
        request.getLongitude(),
        request.getRadiusMeters());

    List<Store> allStores = storeRepository.findByDeleteFlagFalse();

    return allStores.stream()
        .filter(store -> store.getLatitude() != null && store.getLongitude() != null)
        .map(
            store -> {
              double distance =
                  calculateDistance(
                      request.getLatitude(), request.getLongitude(),
                      store.getLatitude(), store.getLongitude());
              return new StoreWithDistance(store, distance);
            })
        .filter(swd -> swd.distance <= request.getRadiusMeters())
        .sorted(Comparator.comparingDouble(swd -> swd.distance))
        .limit(request.getLimit())
        .map(this::mapToNearbyResponse)
        .collect(Collectors.toList());
  }

  private NearbyStoreResponse mapToNearbyResponse(StoreWithDistance swd) {
    Store store = swd.store;
    return NearbyStoreResponse.builder()
        .id(store.getId())
        .name(store.getName())
        .address(store.getAddress())
        .phone(store.getContactPhone())
        .latitude(store.getLatitude())
        .longitude(store.getLongitude())
        .distanceMeters(swd.distance)
        .distanceFormatted(formatDistance(swd.distance))
        .isActive(!store.getDeleteFlag())
        .build();
  }

  /** Calculate distance between two points using Haversine formula. */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_METERS * c;
  }

  private String formatDistance(double meters) {
    if (meters < 1000) {
      return String.format("%.0f m", meters);
    }
    return String.format("%.1f km", meters / 1000);
  }

  private record StoreWithDistance(Store store, double distance) {}
}
