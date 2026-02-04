package com.huynqb.laundrylockerbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LaundryLockerBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(LaundryLockerBackendApplication.class, args);
  }
}
