package com.huynqb.laundrylockerbackend.core.config;

import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.Permission;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.repository.PermissionRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  @Override
  public void run(String... args) throws Exception {
    initializePermissions();
    initializeRoles();
  }

  private void initializePermissions() {
    if (permissionRepository.count() == 0) {
      log.info("Initializing permissions.. .");

      Permission readPermission =
          Permission.builder().name("READ_PRIVILEGE").description("Read permission").build();

      Permission writePermission =
          Permission.builder().name("WRITE_PRIVILEGE").description("Write permission").build();

      Permission deletePermission =
          Permission.builder().name("DELETE_PRIVILEGE").description("Delete permission").build();

      permissionRepository.save(readPermission);
      permissionRepository.save(writePermission);
      permissionRepository.save(deletePermission);

      log.info("Permissions initialized successfully!");
    }
  }

  private void initializeRoles() {
    if (roleRepository.count() == 0) {
      log.info("Initializing roles...");

      Permission readPermission = permissionRepository.findByName("READ_PRIVILEGE").orElseThrow();
      Permission writePermission = permissionRepository.findByName("WRITE_PRIVILEGE").orElseThrow();
      Permission deletePermission =
          permissionRepository.findByName("DELETE_PRIVILEGE").orElseThrow();

      // USER Role - READ only
      Set<Permission> userPermissions = new HashSet<>();
      userPermissions.add(readPermission);

      Role userRole = Role.builder().name(RoleName.USER).permissions(userPermissions).build();

      // MODERATOR Role - READ & WRITE
      Set<Permission> moderatorPermissions = new HashSet<>();
      moderatorPermissions.add(readPermission);
      moderatorPermissions.add(writePermission);

      Role moderatorRole =
          Role.builder().name(RoleName.MODERATOR).permissions(moderatorPermissions).build();

      // ADMIN Role - ALL permissions
      Set<Permission> adminPermissions = new HashSet<>();
      adminPermissions.add(readPermission);
      adminPermissions.add(writePermission);
      adminPermissions.add(deletePermission);

      Role adminRole = Role.builder().name(RoleName.ADMIN).permissions(adminPermissions).build();

      roleRepository.save(userRole);
      roleRepository.save(moderatorRole);
      roleRepository.save(adminRole);

      log.info("Roles initialized successfully!");
    }
  }
}
