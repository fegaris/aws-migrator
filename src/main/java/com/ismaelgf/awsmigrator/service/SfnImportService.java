package com.ismaelgf.awsmigrator.service;

import static com.ismaelgf.awsmigrator.constant.Constants.LOCALSTACK_ACCOUNT_ID;
import static com.ismaelgf.awsmigrator.constant.Constants.SFN_PREFIX;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.CreateStateMachineRequest;
import software.amazon.awssdk.services.sfn.model.DescribeStateMachineRequest;
import software.amazon.awssdk.services.sfn.model.StateMachineListItem;

@Slf4j
@RequiredArgsConstructor
@Service
public class SfnImportService implements AwsImportService {

  @Qualifier("localSfnClient")
  private final SfnClient localSfnClient;

  @Qualifier("sfnClient")
  private final SfnClient sfnClient;

  @Override
  public AwsImportType getType() {
    return AwsImportType.SFN;
  }

  @Override
  public void importService(ApplicationArguments args) {
    log.info("Migrating step functions");

    filterSfns(sfnClient.listStateMachines().stateMachines(), args).forEach(stateMachine -> {
      log.info("Sfn arn: {}", stateMachine.stateMachineArn());
      try {
        var describeStateMachineResponse =
            sfnClient.describeStateMachine(DescribeStateMachineRequest.builder()
                .stateMachineArn(stateMachine.stateMachineArn()).build());
        var definition = describeStateMachineResponse.definition()
            .replaceAll("(?<=:)(\\d{12})(?=:)", LOCALSTACK_ACCOUNT_ID)
            .replaceAll("(?<=/)(\\d{12})(?=/)", LOCALSTACK_ACCOUNT_ID)
            .replaceAll(",\\s*\"Comment\"\\s*:\\s*\".*?\"(?=,|\\})", "");

        var response = localSfnClient.createStateMachine(CreateStateMachineRequest.builder()
            .name(stateMachine.name())
            .roleArn(describeStateMachineResponse.roleArn())
            .type(stateMachine.type())
            .definition(definition)
            .build());

        log.info("Created stateMchine: {}", response.stateMachineArn());
      } catch (Exception e) {
        log.error("Error creating the state machine", e);
      }
    });


  }

  private List<StateMachineListItem> filterSfns(List<StateMachineListItem> list,
      ApplicationArguments args) {
    return list.stream().filter(stateMachineListItem -> {
      if (args.containsOption(SFN_PREFIX)) {
        return stateMachineListItem.name()
            .startsWith(args.getOptionValues(SFN_PREFIX).get(0));
      }
      return true;
    }).toList();
  }
}
