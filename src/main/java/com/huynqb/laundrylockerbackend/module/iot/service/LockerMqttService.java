package com.huynqb.laundrylockerbackend.module.iot.service;

import com.huynqb.laundrylockerbackend.core.config.MqttConfig;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.stereotype.Service;

/**
 * Service for sending MQTT commands to IoT locker devices (ESP8266). Publishes unlock/lock commands
 * to MQTT broker for physical locker control.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LockerMqttService {

  private final MqttClient mqttClient;
  private final MqttConfig mqttConfig;

  /**
   * Send unlock (OPEN) command to ESP8266 via MQTT.
   *
   * @param deviceId Device ID of ESP8266 (e.g., "ESP8266_LOCKER_01")
   * @param boxId Box ID to unlock
   */
  public void sendUnlockCommand(String deviceId, int boxId) {
    String topic = mqttConfig.getTopicPrefix() + "/commands/" + deviceId;
    String payload = String.format("{\"box_id\": %d, \"action\": \"OPEN\"}", boxId);

    publishMessage(topic, payload);
    log.info("[MQTT] Sent OPEN command to {} box {}", deviceId, boxId);
  }

  /**
   * Send lock command to ESP8266 via MQTT.
   *
   * @param deviceId Device ID of ESP8266
   * @param boxId Box ID to lock
   */
  public void sendLockCommand(String deviceId, int boxId) {
    String topic = mqttConfig.getTopicPrefix() + "/commands/" + deviceId;
    String payload = String.format("{\"box_id\": %d, \"action\": \"LOCK\"}", boxId);

    publishMessage(topic, payload);
    log.info("[MQTT] Sent LOCK command to {} box {}", deviceId, boxId);
  }

  /**
   * Publish a message to the MQTT broker.
   *
   * @param topic MQTT topic
   * @param payload JSON payload string
   */
  private void publishMessage(String topic, String payload) {
    try {
      if (!mqttClient.isConnected()) {
        log.warn("[MQTT] Client not connected, attempting reconnect...");
        mqttClient.reconnect();
      }

      MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
      message.setQos(1);
      mqttClient.publish(topic, message);
    } catch (MqttException e) {
      log.error("[MQTT] Failed to publish message to topic: {} - Error: {}", topic, e.toString());
      throw new RuntimeException("MQTT publish failed", e);
    }
  }
}
