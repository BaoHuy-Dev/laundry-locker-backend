package com.huynqb.laundrylockerbackend.core.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Firebase Admin SDK Configuration. Initializes Firebase on application startup using service
 * account credentials.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

  @Value("${firebase.credentials.path:}")
  private String firebaseCredentialsPath;

  @Value("${firebase.enabled:true}")
  private boolean firebaseEnabled;

  private final ResourceLoader resourceLoader;

  public FirebaseConfig(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void initialize() {
    if (!firebaseEnabled) {
      log.info("Firebase is disabled via configuration");
      return;
    }

    if (firebaseCredentialsPath == null || firebaseCredentialsPath.isEmpty()) {
      log.warn("Firebase credentials path not configured. Firebase features will be disabled.");
      return;
    }

    try {
      if (FirebaseApp.getApps().isEmpty()) {
        Resource resource = resourceLoader.getResource(firebaseCredentialsPath);
        if (!resource.exists()) {
          log.warn(
              "Firebase credentials file not found at {}. Firebase features will be disabled.",
              firebaseCredentialsPath);
          return;
        }
        InputStream serviceAccount = resource.getInputStream();

        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase Admin SDK initialized successfully");
      }
    } catch (IOException e) {
      log.error(
          "Failed to initialize Firebase Admin SDK: {}. Firebase features will be disabled.",
          e.getMessage());
    }
  }
}
