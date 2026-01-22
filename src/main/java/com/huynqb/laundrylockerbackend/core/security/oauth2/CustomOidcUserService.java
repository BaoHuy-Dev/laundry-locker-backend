package com.huynqb.laundrylockerbackend.core.security.oauth2;

import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import com.huynqb.laundrylockerbackend.module.user.service.CustomOidcUser;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Override
  @Transactional
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
    log.info("==== Starting OIDC User Loading ====");
    OidcUser oidcUser = super.loadUser(userRequest);

    return processOidcUser(userRequest, oidcUser);
  }

  private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    log.info("Processing OIDC user from provider: {}", registrationId);
    log.info("OIDC attributes: {}", oidcUser.getAttributes());

    String email = oidcUser.getEmail();
    String name = oidcUser.getFullName();
    String picture = oidcUser.getPicture();
    String sub = oidcUser.getSubject();

    log.info("Extracted OIDC info - Email: {}, Name: {}, Sub: {}", email, name, sub);

    if (!StringUtils.hasText(email)) {
      log.error("Email not found from OIDC provider");
      throw new OAuth2AuthenticationException("Email not found from OIDC provider");
    }

    Optional<User> userOptional = userRepository.findByEmail(email);
    User user;

    if (userOptional.isPresent()) {
      log.info("User already exists with email: {}", email);
      user = userOptional.get();

      if (!user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
        log.error(
            "Provider mismatch.  Existing:  {}, Trying: {}", user.getProvider(), registrationId);
        throw new OAuth2AuthenticationException(
            "Looks like you're signed up with "
                + user.getProvider()
                + " account. "
                + "Please use your "
                + user.getProvider()
                + " account to login.");
      }

      user = updateExistingUser(user, name, picture);
    } else {
      log.info("Creating new user with email: {}", email);
      user = registerNewUser(registrationId, email, name, picture, sub);
    }

    log.info("User processing completed. User ID: {}, Email: {}", user.getId(), user.getEmail());
    return new CustomOidcUser(oidcUser, user);
  }

  private User registerNewUser(
      String registrationId, String email, String name, String picture, String providerId) {
    log.info("==== Registering New OIDC User ====");

    Role userRole =
        roleRepository
            .findByName(RoleName.USER)
            .orElseThrow(
                () -> {
                  log.error("USER role not found in database!");
                  return new OAuth2AuthenticationException(
                      "User role not configured. Please contact administrator.");
                });

    log.info("Found USER role with ID: {}", userRole.getId());

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    String providerName = registrationId.toUpperCase();
    log.info("Provider name: {}", providerName);

    User user =
        User.builder()
            .name(name)
            .email(email)
            .imageUrl(picture)
            .provider(AuthProvider.valueOf(providerName))
            .providerId(providerId)
            .emailVerified(true)
            .roles(roles)
            .build();

    log.info("Built user object: {}", user.getEmail());

    User savedUser = userRepository.save(user);
    log.info("User saved to database with ID: {}", savedUser.getId());

    return savedUser;
  }

  private User updateExistingUser(User existingUser, String name, String picture) {
    log.info("Updating existing user:  {}", existingUser.getEmail());

    existingUser.setName(name);
    existingUser.setImageUrl(picture);

    User updatedUser = userRepository.save(existingUser);
    log.info("User updated successfully");

    return updatedUser;
  }
}
