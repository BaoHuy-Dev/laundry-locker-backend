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

  @Value("${firebase.credentials.path}")
  private String firebaseCredentialsPath;

  private final ResourceLoader resourceLoader;

  public FirebaseConfig(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void initialize() {
    try {
      if (FirebaseApp.getApps().isEmpty()) {
        Resource resource = resourceLoader.getResource(firebaseCredentialsPath);
        InputStream serviceAccount = resource.getInputStream();

        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase Admin SDK initialized successfully");
      }
    } catch (IOException e) {
      log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
      throw new RuntimeException("Could not initialize Firebase", e);
    }
  }
}
