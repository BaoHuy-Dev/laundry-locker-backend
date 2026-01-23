package com.huynqb.laundrylockerbackend.module.laundry.repository;

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
}
