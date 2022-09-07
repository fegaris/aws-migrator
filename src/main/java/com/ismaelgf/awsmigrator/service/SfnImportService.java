package com.ismaelgf.awsmigrator.service;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.CreateStateMachineRequest;
import software.amazon.awssdk.services.sfn.model.DescribeStateMachineRequest;

@Slf4j
@AllArgsConstructor
@Service
public class SfnImportService implements AwsImportService{

  @Qualifier("localSfnClient")
  private final SfnClient localSfnClient;

  private final SfnClient sfnClient;

  @Override
  public AwsImportType getType() {
    return AwsImportType.SFN;
  }

  @Override
  public void importService(ApplicationArguments args) {
    log.info("Migrating step functions");

    sfnClient.listStateMachines().stateMachines().forEach(stateMachine -> {
      log.info("Sfn arn: {}", stateMachine.stateMachineArn());
      var describeStateMachineResponse =
          sfnClient.describeStateMachine(DescribeStateMachineRequest.builder().stateMachineArn(stateMachine.stateMachineArn()).build());
      var response = localSfnClient.createStateMachine(CreateStateMachineRequest.builder()
              .name(stateMachine.name())
              .roleArn(stateMachine.stateMachineArn())
              .type(stateMachine.type())
              .definition(describeStateMachineResponse.definition())
          .build());

      log.info("Created stateMchine: {}", response.stateMachineArn());
    });


  }
}
