package com.huynqb.laundrylockerbackend.core.security.oauth2;

import java.util.Map;

/** OAuth2UserInfo implementation for GitHub. */
public class GithubOAuth2UserInfo extends OAuth2UserInfo {

  public GithubOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    Object id = attributes.get("id");
    return id != null ? id.toString() : null;
  }

  @Override
  public String getName() {
    String name = (String) attributes.get("name");
    // Fallback to login if name is not available
    return name != null ? name : (String) attributes.get("login");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }

  @Override
  public String getImageUrl() {
    return (String) attributes.get("avatar_url");
  }
}
