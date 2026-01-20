package com.huynqb.laundrylockerbackend.core.config;

import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceStatus;
import com.huynqb.laundrylockerbackend.module.laundry.model.LaundryService;
import com.huynqb.laundrylockerbackend.module.laundry.repository.LaundryServiceRepository;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.enums.LockerStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerRepository;
import com.huynqb.laundrylockerbackend.module.store.enums.StoreStatus;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.Permission;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.repository.PermissionRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Data initializer for seed data. Creates roles, permissions, and sample store/locker data. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final StoreRepository storeRepository;
  private final LockerRepository lockerRepository;
  private final BoxRepository boxRepository;
  private final LaundryServiceRepository laundryServiceRepository;

  @Override
  public void run(String... args) throws Exception {
    initializePermissions();
    initializeRoles();
    initializeStoreData();
  }

  private void initializePermissions() {
    if (permissionRepository.count() == 0) {
      log.info("Initializing permissions...");

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

      // STAFF Role - READ & WRITE
      Set<Permission> staffPermissions = new HashSet<>();
      staffPermissions.add(readPermission);
      staffPermissions.add(writePermission);
      Role staffRole = Role.builder().name(RoleName.STAFF).permissions(staffPermissions).build();

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
      roleRepository.save(staffRole);
      roleRepository.save(moderatorRole);
      roleRepository.save(adminRole);

      log.info("Roles initialized successfully!");
    }
  }

  private void initializeStoreData() {
    if (storeRepository.count() == 0) {
      log.info("Initializing store and locker data...");

      // Create Sample Store
      Store store =
          Store.builder()
              .name("Laundry Store District 1")
              .contactPhone("+84911649183")
              .status(StoreStatus.ACTIVE)
              .address("123 Nguyen Hue, District 1, HCMC")
              .longitude(106.7009)
              .latitude(10.7769)
              .description("Main laundry store in District 1")
              .build();
      Store savedStore = storeRepository.save(store);

      // Create Sample Locker
      Locker locker =
          Locker.builder()
              .code("LCK-001")
              .name("Locker Nguyen Hue")
              .status(LockerStatus.ACTIVE)
              .address("123 Nguyen Hue, District 1")
              .longitude(106.7009)
              .latitude(10.7769)
              .description("Smart locker at Nguyen Hue street")
              .store(savedStore)
              .build();
      Locker savedLocker = lockerRepository.save(locker);

      // Create Sample Boxes (5 boxes)
      for (int i = 1; i <= 5; i++) {
        Box box =
            Box.builder()
                .boxNumber(i)
                .isActive(true)
                .status(BoxStatus.AVAILABLE)
                .description("Box " + i)
                .locker(savedLocker)
                .build();
        boxRepository.save(box);
      }

      // Create Sample Services
      LaundryService washService =
          LaundryService.builder()
              .name("Wash & Fold")
              .price(new BigDecimal("50000"))
              .unit("kg")
              .status(ServiceStatus.ACTIVE)
              .description("Basic wash and fold service")
              .store(savedStore)
              .build();
      laundryServiceRepository.save(washService);

      LaundryService dryCleanService =
          LaundryService.builder()
              .name("Dry Cleaning")
              .price(new BigDecimal("100000"))
              .unit("piece")
              .status(ServiceStatus.ACTIVE)
              .description("Professional dry cleaning service")
              .store(savedStore)
              .build();
      laundryServiceRepository.save(dryCleanService);

      LaundryService expressService =
          LaundryService.builder()
              .name("Express Laundry")
              .price(new BigDecimal("80000"))
              .unit("kg")
              .status(ServiceStatus.ACTIVE)
              .description("Express 3-hour laundry service")
              .store(savedStore)
              .build();
      laundryServiceRepository.save(expressService);

      log.info("Store and locker data initialized successfully!");
      log.info("Created: 1 Store, 1 Locker, 5 Boxes, 3 Services");
    }
  }
}
