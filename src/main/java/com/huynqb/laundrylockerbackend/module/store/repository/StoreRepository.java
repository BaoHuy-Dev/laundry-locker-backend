package com.huynqb.laundrylockerbackend.module.store.repository;

import com.huynqb.laundrylockerbackend.module.store.enums.StoreStatus;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Store entity. */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

  List<Store> findByStatus(StoreStatus status);

  List<Store> findByDeleteFlagFalse();
}
