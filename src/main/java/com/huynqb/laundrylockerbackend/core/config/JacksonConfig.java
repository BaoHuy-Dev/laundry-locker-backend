package com.huynqb.laundrylockerbackend.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson ObjectMapper configuration. Provides a configured ObjectMapper bean for JSON
 * serialization/deserialization.
 */
@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    // Register Java 8 date/time module for LocalDateTime, etc.
    objectMapper.registerModule(new JavaTimeModule());

    // Write dates as ISO strings instead of timestamps
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    return objectMapper;
  }
}
