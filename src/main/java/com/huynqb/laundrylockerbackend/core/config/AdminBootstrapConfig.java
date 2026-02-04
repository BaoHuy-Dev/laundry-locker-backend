package com.huynqb.laundrylockerbackend.core.config;

import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
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

/**
 * AdminBootstrapConfig - Automatically creates Super Admin user on application startup.
 *
 * <p>Configuration is read from environment variables or application properties:
 *
 * <ul>
 *   <li>app.admin.bootstrap.enabled - Enable/disable bootstrap (default: true)
 *   <li>app.admin.bootstrap.email - Super admin email (required)
 *   <li>app.admin.bootstrap.password - Super admin password (required)
 * </ul>
 *
 * <p>The bootstrap only runs if:
 *
 * <ul>
 *   <li>Bootstrap is enabled
 *   <li>Email and password are configured
 *   <li>No admin with this email exists
 * </ul>
 */
@Slf4j
@Component
@Order(10) // Run after DataInitializer (which creates roles)
@RequiredArgsConstructor
public class AdminBootstrapConfig implements CommandLineRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.admin.bootstrap.enabled:true}")
  private boolean bootstrapEnabled;

  @Value("${app.admin.bootstrap.email:}")
  private String adminEmail;

  @Value("${app.admin.bootstrap.password:}")
  private String adminPassword;

  @Value("${app.admin.bootstrap.name:Super Admin}")
  private String adminName;

  @Override
  public void run(String... args) throws Exception {
    if (!bootstrapEnabled) {
      log.info("Admin bootstrap is disabled");
      return;
    }

    if (adminEmail == null || adminEmail.isBlank()) {
      log.warn(
          "Admin bootstrap email is not configured. Set SUPER_ADMIN_EMAIL or app.admin.bootstrap.email");
      return;
    }

    if (adminPassword == null || adminPassword.isBlank()) {
      log.warn(
          "Admin bootstrap password is not configured. Set SUPER_ADMIN_PASSWORD or app.admin.bootstrap.password");
      return;
    }

    // Check if admin already exists
    if (userRepository.findByEmail(adminEmail.trim().toLowerCase()).isPresent()) {
      log.info("Super Admin already exists: {}", adminEmail);
      return;
    }

    // Ensure ADMIN role exists
    Role adminRole =
        roleRepository
            .findByName(RoleName.ADMIN)
            .orElseGet(
                () -> {
                  log.info("Creating ADMIN role...");
                  return roleRepository.save(Role.builder().name(RoleName.ADMIN).build());
                });

    // Create Super Admin user
    Set<Role> roles = new HashSet<>();
    roles.add(adminRole);

    User superAdmin =
        User.builder()
            .email(adminEmail.trim().toLowerCase())
            .password(passwordEncoder.encode(adminPassword))
            .name(adminName)
            .firstName("Super")
            .lastName("Admin")
            .provider(AuthProvider.LOCAL)
            .providerId("super_admin")
            .emailVerified(true)
            .phoneVerified(false)
            .enabled(true)
            .roles(roles)
            .build();

    userRepository.save(superAdmin);

    log.info("╔════════════════════════════════════════════════════════════╗");
    log.info("║              ✅ SUPER ADMIN CREATED SUCCESSFULLY           ║");
    log.info("╠════════════════════════════════════════════════════════════╣");
    log.info("║  Email: {}", padRight(adminEmail, 50) + "║");
    log.info("║  Name:  {}", padRight(adminName, 50) + "║");
    log.info("╚════════════════════════════════════════════════════════════╝");
  }

  private String padRight(String s, int n) {
    if (s.length() >= n) {
      return s.substring(0, n - 3) + "...";
    }
    return String.format("%-" + n + "s", s);
  }
}
