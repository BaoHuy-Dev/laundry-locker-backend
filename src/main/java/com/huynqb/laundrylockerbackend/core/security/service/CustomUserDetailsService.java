package com.huynqb.laundrylockerbackend.core.security.service;

import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Custom UserDetailsService for Spring Security authentication */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.debug("Loading user by email: {}", email);

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

    log.debug("User found: id={}, email={}", user.getId(), user.getEmail());
    log.debug("User roles count: {}", user.getRoles() != null ? user.getRoles().size() : 0);

    // Log each role
    if (user.getRoles() != null) {
      user.getRoles()
          .forEach(role -> log.debug("User role: {} (id={})", role.getName(), role.getId()));
    }

    // Build authorities
    Collection<GrantedAuthority> authorities =
        user.getRoles().stream()
            .map(Role::getName)
            .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName.name()))
            .collect(Collectors.toList());

    log.info("User {} authorities: {}", email, authorities);

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(user.getPassword() != null ? user.getPassword() : "")
        .authorities(authorities)
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }
}
