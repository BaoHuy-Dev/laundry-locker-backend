package com.huynqb.laundrylockerbackend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/** WebSocket configuration for real-time notifications. Uses STOMP protocol over WebSocket. */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable simple broker for broadcasting to subscribers
    // /topic for broadcast messages, /queue for user-specific messages
    config.enableSimpleBroker("/topic", "/queue");

    // Application destination prefix for @MessageMapping methods
    config.setApplicationDestinationPrefixes("/app");

    // User destination prefix for user-specific messages
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // WebSocket endpoint that clients will connect to
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .withSockJS(); // Fallback for browsers without WebSocket support

    // Also register without SockJS for native WebSocket clients
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
  }
}
