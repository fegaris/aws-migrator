package com.ismaelgf.awsmigrator.config;

import static com.ismaelgf.awsmigrator.constant.Constants.LOCALHOST;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class AmazonDynamodbConfiguration {

  @Bean("localDynamoDbClient")
  public DynamoDbClient createLocalClient() throws URISyntaxException {
    return DynamoDbClient.builder().endpointOverride(new URI(LOCALHOST)).build();
  }

  @Bean("dynamoDbClient")
  public DynamoDbClient createClient() {
    return DynamoDbClient.create();
  }

}
