package com.ismaelgf.awsmigrator.config;

import static com.ismaelgf.awsmigrator.constant.Constants.LOCALHOST;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sfn.SfnClient;

@Configuration
public class AmazonSfnConfiguration {

  @Bean("localSfnClient")
  public SfnClient createLocalSfnClient() throws URISyntaxException {
    return SfnClient.builder().endpointOverride(new URI(LOCALHOST)).build();
  }

  @Bean("sfnClient")
  public SfnClient createSfnClient() {
    return SfnClient.create();
  }

}
