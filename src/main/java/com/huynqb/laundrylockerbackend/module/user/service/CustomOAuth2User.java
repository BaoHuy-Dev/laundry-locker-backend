package com.huynqb.laundrylockerbackend.module.user.service;

import com.huynqb.laundrylockerbackend.module.user.model.User;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class CustomOAuth2User implements OAuth2User {

  private final OAuth2User oauth2User;
  private final User user;

  public CustomOAuth2User(OAuth2User oauth2User, User user) {
    this.oauth2User = oauth2User;
    this.user = user;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return oauth2User.getAttributes();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();

    // Add roles
    user.getRoles()
        .forEach(
            role -> {
              authorities.add(new SimpleGrantedAuthority(role.getName().name()));

              // Add permissions from each role
              role.getPermissions()
                  .forEach(
                      permission ->
                          authorities.add(new SimpleGrantedAuthority(permission.getName())));
            });

    return authorities;
  }

  @Override
  public String getName() {
    return oauth2User.getAttribute("name");
  }

  public String getEmail() {
    return oauth2User.getAttribute("email");
  }
}
