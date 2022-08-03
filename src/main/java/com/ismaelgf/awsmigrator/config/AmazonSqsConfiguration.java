package com.ismaelgf.awsmigrator.config;

import static com.ismaelgf.awsmigrator.constant.Constants.LOCALHOST;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AmazonSqsConfiguration {

  @Bean("localSqsClient")
  public SqsClient createLocalClient() throws URISyntaxException {
    return SqsClient.builder().endpointOverride(new URI(LOCALHOST)).build();
  }

  @Bean("sqsClient")
  public SqsClient createClient() {
    return SqsClient.create();
  }
}
