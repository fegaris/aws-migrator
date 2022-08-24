package com.ismaelgf.awsmigrator.service;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.CreateStateMachineRequest;

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

    //TODO get definition

    sfnClient.listStateMachines().stateMachines().forEach(stateMachineListItem -> {
      log.info("Sfn arn: {}", stateMachineListItem.stateMachineArn());
      var response = localSfnClient.createStateMachine(CreateStateMachineRequest.builder()
              .name(stateMachineListItem.name())
              .roleArn(stateMachineListItem.stateMachineArn())
              .type(stateMachineListItem.type())
//              .definition()
          .build());

      log.info("Created stateMchine: {}", response.stateMachineArn());
    });


  }
}
