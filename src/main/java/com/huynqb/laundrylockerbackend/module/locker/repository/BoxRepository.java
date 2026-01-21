package com.huynqb.laundrylockerbackend.module.locker.repository;

import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Box entity. */
@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {

  List<Box> findByLockerId(Long lockerId);

  List<Box> findByLockerIdAndStatus(Long lockerId, BoxStatus status);

  Optional<Box> findByLockerIdAndBoxNumber(Long lockerId, Integer boxNumber);

  List<Box> findByLockerIdAndIsActiveTrue(Long lockerId);

  Optional<Box> findFirstByLockerIdAndStatusAndIsActiveTrue(Long lockerId, BoxStatus status);
}
