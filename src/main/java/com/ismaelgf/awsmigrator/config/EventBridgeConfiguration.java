package com.ismaelgf.awsmigrator.config;

import static com.ismaelgf.awsmigrator.constant.Constants.LOCALHOST;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

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
