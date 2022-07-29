package com.ismaelgf.awsmigrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.net.URI;
import java.net.URISyntaxException;

import static com.ismaelgf.awsmigrator.constant.Constants.LOCALHOST;

@Configuration
public class EventBridgeConfiguration {

  @Bean("localEventBridgeClient")
  public EventBridgeClient createLocalEventBridgeClient() throws URISyntaxException {
    return EventBridgeClient.builder().endpointOverride(new URI(LOCALHOST)).build();
  }

  @Bean("eventBridgeClient")
  public EventBridgeClient createEventBridgeClient() {
    return EventBridgeClient.create();
  }
}
