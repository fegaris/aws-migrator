package com.ismaelgf.awsmigrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class EventBridgeConfiguration {

    private static final String LOCALHOST = "http://localhost:4566";

    @Bean("localEventBridgeClient")
    public EventBridgeClient createLocalEventBridgeClient() throws URISyntaxException {
        return EventBridgeClient.builder().endpointOverride(new URI(LOCALHOST)).build();
    }

    @Bean("eventBridgeClient")
    public EventBridgeClient createEventBridgeClient() throws URISyntaxException {
        return EventBridgeClient.create();
    }

}
