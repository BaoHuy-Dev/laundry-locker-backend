package com.huynqb.laundrylockerbackend.core.config;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * SchemaInitializer - Ensures the database schema exists before Hibernate runs.
 *
 * <p>This solves the issue where Docker volume already exists but schema was not created,
 * causing Hibernate to fail when trying to create tables in non-existent schema.
 *
 * <p>This runs at @PostConstruct (before CommandLineRunner) to ensure schema exists
 * before any entity operations.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SchemaInitializer {

  private final DataSource dataSource;

  @Value("${spring.jpa.properties.hibernate.default_schema:laundry_locker_schema}")
  private String schemaName;

  @PostConstruct
  public void initSchema() {
    try {
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

      // Check if schema exists
      String checkSql =
          "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)";
      Boolean schemaExists = jdbcTemplate.queryForObject(checkSql, Boolean.class, schemaName);

      if (Boolean.FALSE.equals(schemaExists)) {
        log.info("📦 Schema '{}' does not exist. Creating...", schemaName);

        // Create schema
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);

        // Grant privileges
        jdbcTemplate.execute(
            "GRANT ALL PRIVILEGES ON SCHEMA " + schemaName + " TO CURRENT_USER");

        log.info("✅ Schema '{}' created successfully!", schemaName);
      } else {
        log.debug("Schema '{}' already exists.", schemaName);
      }

      // Set search path for current connection
      jdbcTemplate.execute("SET search_path TO " + schemaName + ", public");

    } catch (Exception e) {
      log.error("❌ Failed to initialize schema '{}': {}", schemaName, e.getMessage());
      // Don't throw - let Hibernate handle if schema doesn't exist
      // This is just a fallback mechanism
    }
  }
}

