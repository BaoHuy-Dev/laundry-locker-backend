package com.huynqb.laundrylockerbackend.module.partner.repository;

import com.huynqb.laundrylockerbackend.module.partner.enums.PartnerStatus;
import com.huynqb.laundrylockerbackend.module.partner.model.Partner;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

  Optional<Partner> findByUserId(Long userId);

  boolean existsByUserId(Long userId);

  Page<Partner> findByStatus(PartnerStatus status, Pageable pageable);

  @Query("SELECT p FROM Partner p WHERE p.status = :status")
  Page<Partner> findAllByStatus(@Param("status") PartnerStatus status, Pageable pageable);

  @Query(
      "SELECT p FROM Partner p WHERE p.businessName LIKE %:keyword% OR p.contactEmail LIKE %:keyword%")
  Page<Partner> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

  @Query("SELECT COUNT(p) FROM Partner p WHERE p.status = :status")
  long countByStatus(@Param("status") PartnerStatus status);
}
