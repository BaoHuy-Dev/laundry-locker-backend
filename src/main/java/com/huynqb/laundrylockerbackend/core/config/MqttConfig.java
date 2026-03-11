package com.huynqb.laundrylockerbackend.core.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT Configuration for IoT locker control. Connects to MQTT broker to publish unlock/lock
 * commands to ESP8266 devices.
 */
@Slf4j
@Getter
@Configuration
public class MqttConfig {

  @Value("${mqtt.broker-url:tcp://broker.hivemq.com:1883}")
  private String brokerUrl;

  @Value("${mqtt.client-id:laundry-backend-${random.uuid}}")
  private String clientId;

  @Value("${mqtt.username:}")
  private String username;

  @Value("${mqtt.password:}")
  private String password;

  @Value("${mqtt.topic-prefix:locker}")
  private String topicPrefix;

  @Bean(destroyMethod = "")
  public MqttClient mqttClient() throws MqttException {
    log.info("[MQTT] Connecting to broker: {}", brokerUrl);

    MqttClient client = new MqttClient(brokerUrl, clientId);
    MqttConnectionOptions options = new MqttConnectionOptions();
    options.setAutomaticReconnect(true);
    options.setCleanStart(true);
    options.setConnectionTimeout(10);
    options.setKeepAliveInterval(60);

    if (username != null && !username.isBlank()) {
      options.setUserName(username);
    }
    if (password != null && !password.isBlank()) {
      options.setPassword(password.getBytes());
    }

    try {
      client.connect(options);
      log.info("[MQTT] Connected to broker: {}", brokerUrl);
    } catch (MqttException e) {
      log.error(
          "[MQTT] Failed to connect to broker: {}. IoT commands will not work. Error: {}",
          brokerUrl,
          e.getMessage());
      // Don't throw - allow application to start even if MQTT broker is unavailable
      // Commands will fail gracefully when attempted
    }

    return client;
  }

  @jakarta.annotation.PreDestroy
  public void cleanup() {
    try {
      MqttClient client = mqttClient();
      if (client != null && client.isConnected()) {
        log.info("[MQTT] Disconnecting from broker before shutdown...");
        client.disconnect();
        client.close();
      }
    } catch (Exception e) {
      log.error("[MQTT] Error during cleanup: {}", e.getMessage());
    }
  }
}
