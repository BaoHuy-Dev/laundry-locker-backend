package com.huynqb.laundrylockerbackend.module.user.repository;

import com.huynqb.laundrylockerbackend.module.user.model.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> findByName(String name);
}
