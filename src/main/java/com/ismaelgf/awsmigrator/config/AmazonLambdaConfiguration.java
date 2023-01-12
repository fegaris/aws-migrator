package com.ismaelgf.awsmigrator.config;

import static com.ismaelgf.awsmigrator.constant.Constants.LOCALHOST;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class AmazonLambdaConfiguration {

  @Bean("localLambdaClient")
  public LambdaClient createLocalEventBridgeClient() throws URISyntaxException {
    return LambdaClient.builder().endpointOverride(new URI(LOCALHOST)).build();
  }

  @Bean("lambdaClient")
  public LambdaClient createEventBridgeClient() {
    return LambdaClient.create();
  }

}
