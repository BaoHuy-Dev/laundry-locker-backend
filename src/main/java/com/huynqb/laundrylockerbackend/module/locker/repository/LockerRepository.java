package com.huynqb.laundrylockerbackend.module.locker.repository;

import com.huynqb.laundrylockerbackend.module.locker.enums.LockerStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Locker entity. */
@Repository
public interface LockerRepository extends JpaRepository<Locker, Long> {

  Optional<Locker> findByCode(String code);

  List<Locker> findByStoreId(Long storeId);

  List<Locker> findByStatus(LockerStatus status);

  List<Locker> findByStoreIdAndStatus(Long storeId, LockerStatus status);

  List<Locker> findByDeleteFlagFalse();
}
