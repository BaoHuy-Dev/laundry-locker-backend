package com.huynqb.laundrylockerbackend.core.security.oauth2;

import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import com.huynqb.laundrylockerbackend.module.user.service.CustomOAuth2User;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    log.info("==== Starting OAuth2 User Loading ====");
    OAuth2User oauth2User = super.loadUser(userRequest);

    return processOAuth2User(userRequest, oauth2User);
  }

  @Transactional
  private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    log.info("Processing OAuth2 user from provider: {}", registrationId);
    log.info("OAuth2 attributes: {}", oauth2User.getAttributes());

    OAuth2UserInfo oAuth2UserInfo =
        OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());

    log.info(
        "Extracted user info - Email: {}, Name: {}, ID: {}",
        oAuth2UserInfo.getEmail(),
        oAuth2UserInfo.getName(),
        oAuth2UserInfo.getId());

    if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
      log.error("Email not found from OAuth2 provider");
      throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
    }

    Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
    User user;

    if (userOptional.isPresent()) {
      log.info("User already exists with email: {}", oAuth2UserInfo.getEmail());
      user = userOptional.get();

      if (!user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
        log.error(
            "Provider mismatch.  Existing:  {}, Trying: {}", user.getProvider(), registrationId);
        throw new OAuth2AuthenticationException(
            "Looks like you're signed up with "
                + user.getProvider()
                + " account.  "
                + "Please use your "
                + user.getProvider()
                + " account to login.");
      }

      user = updateExistingUser(user, oAuth2UserInfo);
    } else {
      log.info("Creating new user with email: {}", oAuth2UserInfo.getEmail());
      user = registerNewUser(userRequest, oAuth2UserInfo);
    }

    log.info("User processing completed.  User ID: {}, Email: {}", user.getId(), user.getEmail());
    return new CustomOAuth2User(oauth2User, user);
  }

  @Transactional
  private User registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo oAuth2UserInfo) {
    log.info("==== Registering New User ====");

    // Get default USER role
    Role userRole =
        roleRepository
            .findByName(RoleName.USER)
            .orElseThrow(
                () -> {
                  log.error("USER role not found in database!");
                  return new RuntimeException(
                      "User Role not found.  Please run DataInitializer first.");
                });

    log.info("Found USER role with ID: {}", userRole.getId());

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    String providerName = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
    log.info("Provider name: {}", providerName);

    User user =
        User.builder()
            .name(oAuth2UserInfo.getName())
            .email(oAuth2UserInfo.getEmail())
            .imageUrl(oAuth2UserInfo.getImageUrl())
            .provider(AuthProvider.valueOf(providerName))
            .providerId(oAuth2UserInfo.getId())
            .emailVerified(true)
            .roles(roles)
            .build();

    log.info("Built user object: {}", user.getEmail());

    User savedUser = userRepository.save(user);
    log.info("User saved to database with ID: {}", savedUser.getId());

    return savedUser;
  }

  @Transactional
  private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
    log.info("Updating existing user:  {}", existingUser.getEmail());

    existingUser.setName(oAuth2UserInfo.getName());
    existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());

    User updatedUser = userRepository.save(existingUser);
    log.info("User updated successfully");

    return updatedUser;
  }
}
