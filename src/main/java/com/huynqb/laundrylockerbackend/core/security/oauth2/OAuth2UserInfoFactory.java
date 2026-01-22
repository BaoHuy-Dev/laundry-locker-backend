package com.huynqb.laundrylockerbackend.core.security.oauth2;

import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

/**
 * Factory class to create appropriate OAuth2UserInfo based on the OAuth2 provider.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuth2UserInfoFactory {

  /**
   * Get OAuth2UserInfo based on the registration ID (provider name).
   *
   * @param registrationId The OAuth2 provider registration ID (e.g., "google", "facebook")
   * @param attributes The user attributes from OAuth2 provider
   * @return OAuth2UserInfo implementation for the specific provider
   * @throws OAuth2AuthenticationException if provider is not supported
   */
  public static OAuth2UserInfo getOAuth2UserInfo(
      String registrationId, Map<String, Object> attributes) {

    if (registrationId == null) {
      throw new OAuth2AuthenticationException("Registration ID cannot be null");
    }

    String provider = registrationId.toUpperCase();

    if (provider.equals(AuthProvider.GOOGLE.name())) {
      return new GoogleOAuth2UserInfo(attributes);
    } else if (provider.equals(AuthProvider.FACEBOOK.name())) {
      return new FacebookOAuth2UserInfo(attributes);
    } else if (provider.equals(AuthProvider.GITHUB.name())) {
      return new GithubOAuth2UserInfo(attributes);
    } else if (provider.equals(AuthProvider.ZALO.name())) {
      return new ZaloOAuth2UserInfo(attributes);
    }

    throw new OAuth2AuthenticationException(
        "Login with " + registrationId + " is not supported. " +
        "Supported providers: Google, Facebook, GitHub, Zalo");
  }
}
