package com.huynqb.laundrylockerbackend.module.user.service;

import com.huynqb.laundrylockerbackend.module.user.model.User;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Getter
public class CustomOidcUser implements OidcUser {

  private final OidcUser oidcUser;
  private final User user;

  public CustomOidcUser(OidcUser oidcUser, User user) {
    this.oidcUser = oidcUser;
    this.user = user;
  }

  @Override
  public Map<String, Object> getClaims() {
    return oidcUser.getClaims();
  }

  @Override
  public OidcUserInfo getUserInfo() {
    return oidcUser.getUserInfo();
  }

  @Override
  public OidcIdToken getIdToken() {
    return oidcUser.getIdToken();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return oidcUser.getAttributes();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();

    // Add roles from database
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
    return oidcUser.getFullName();
  }
}
