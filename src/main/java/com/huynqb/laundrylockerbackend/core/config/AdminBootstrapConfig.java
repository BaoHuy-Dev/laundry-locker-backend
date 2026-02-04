package com.huynqb.laundrylockerbackend.core.config;

import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin Bootstrap Configuration. Creates or updates the super admin user on application startup.
 * Only runs when admin.bootstrap.enabled=true.
 */
@Slf4j
@Component
@Order(2) // Run after DataInitializer
@RequiredArgsConstructor
public class AdminBootstrapConfig implements CommandLineRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.admin.bootstrap.enabled:false}")
  private boolean bootstrapEnabled;

  @Value("${app.admin.bootstrap.email:admin@example.com}")
  private String adminEmail;

  @Value("${app.admin.bootstrap.password:Admin@123456}")
  private String adminPassword;

  @Value("${app.admin.bootstrap.name:Super Admin}")
  private String adminName;

  @Override
  @Transactional
  public void run(String... args) {
    if (!bootstrapEnabled) {
      log.info("Admin bootstrap is disabled. Set app.admin.bootstrap.enabled=true to enable.");
      return;
    }

    log.info("Admin bootstrap enabled. Processing admin user...");
    createOrUpdateAdmin();
  }

  private void createOrUpdateAdmin() {
    // Find or create admin user
    User adminUser = userRepository.findByEmail(adminEmail).orElse(null);

    // Get ADMIN role
    Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElse(null);

    if (adminRole == null) {
      log.error("ADMIN role not found. Please ensure DataInitializer runs first.");
      return;
    }

    if (adminUser == null) {
      // Create new admin user
      Set<Role> roles = new HashSet<>();
      roles.add(adminRole);

      adminUser =
          User.builder()
              .email(adminEmail)
              .password(passwordEncoder.encode(adminPassword))
              .name(adminName)
              .emailVerified(true)
              .roles(roles)
              .build();

      userRepository.save(adminUser);
      log.info("Admin user CREATED: {}", adminEmail);
    } else {
      // Update existing admin user's password and ensure ADMIN role
      adminUser.setPassword(passwordEncoder.encode(adminPassword));
      adminUser.setEmailVerified(true);

      if (adminUser.getRoles() == null) {
        adminUser.setRoles(new HashSet<>());
      }
      adminUser.getRoles().add(adminRole);

      userRepository.save(adminUser);
      log.info("Admin user UPDATED: {}", adminEmail);
    }
  }
}
