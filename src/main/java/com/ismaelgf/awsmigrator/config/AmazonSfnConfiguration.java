package com.ismaelgf.awsmigrator.config;

import static com.ismaelgf.awsmigrator.constant.Constants.LOCALHOST;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.sfn.SfnClient;

@Configuration
public class AmazonSfnConfiguration {

  @Bean("localSfnClient")
  public SfnClient createLocalEventBridgeClient() throws URISyntaxException {
    return SfnClient.builder().endpointOverride(new URI(LOCALHOST)).build();
  }

  @Primary
  @Bean("eventSfnClient")
  public SfnClient createEventBridgeClient() {
    return SfnClient.create();
  }

}
