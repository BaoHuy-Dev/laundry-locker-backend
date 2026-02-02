package com.huynqb.laundrylockerbackend.module.laundry.repository;

import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceCategory;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceStatus;
import com.huynqb.laundrylockerbackend.module.laundry.model.LaundryService;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for LaundryService entity. */
@Repository
public interface LaundryServiceRepository extends JpaRepository<LaundryService, Long> {

  List<LaundryService> findByStoreId(Long storeId);

  List<LaundryService> findByStoreIdAndStatus(Long storeId, ServiceStatus status);

  List<LaundryService> findByDeleteFlagFalse();

  List<LaundryService> findByStatus(ServiceStatus status);

  /** Find services by category (STORAGE or LAUNDRY). */
  List<LaundryService> findByCategoryAndDeleteFlagFalse(ServiceCategory category);

  /** Find services by category and status. */
  List<LaundryService> findByCategoryAndStatusAndDeleteFlagFalse(
      ServiceCategory category, ServiceStatus status);

  /** Find services by store and category. */
  List<LaundryService> findByStoreIdAndCategoryAndStatusAndDeleteFlagFalse(
      Long storeId, ServiceCategory category, ServiceStatus status);
}
