package com.huynqb.laundrylockerbackend.module.locker.repository;

import com.huynqb.laundrylockerbackend.module.locker.model.LockerReport;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LockerReportRepository extends JpaRepository<LockerReport, Long> {
  Page<LockerReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

  List<LockerReport> findByUserIdOrderByCreatedAtDesc(Long userId);
}
