package com.ismaelgf.awsmigrator.service;

import static com.ismaelgf.awsmigrator.constant.Constants.LOCALSTACK_ACCOUNT_ID;
import static com.ismaelgf.awsmigrator.constant.Constants.SFN_PREFIX;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
            .replaceAll("(?<=/)(\\d{12})(?=/)", LOCALSTACK_ACCOUNT_ID);

        definition = removeComments(definition);
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


  public static String removeComments(String jsonString) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(jsonString);
    removeCommentsRecursive(rootNode);
    return mapper.writeValueAsString(rootNode);
  }

  private static void removeCommentsRecursive(JsonNode node) {
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      Iterator<Entry<String, JsonNode>> fields = objectNode.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> entry = fields.next();
        if (entry.getKey().equals("Comment")) {
          fields.remove();
        } else {
          removeCommentsRecursive(entry.getValue());
        }
      }
    } else if (node.isArray()) {
      for (JsonNode item : node) {
        removeCommentsRecursive(item);
      }
    }
  }

}
