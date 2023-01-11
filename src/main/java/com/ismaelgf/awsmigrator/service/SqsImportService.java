package com.ismaelgf.awsmigrator.service;

import static com.ismaelgf.awsmigrator.constant.Constants.SQS_PREFIX;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;

@Service
@AllArgsConstructor
@Slf4j
public class SqsImportService implements AwsImportService {

  @Qualifier("localSqsClient")
  private final SqsClient localSqsClient;

  @Qualifier("sqsClient")
  private final SqsClient sqsClient;

  @Override
  public AwsImportType getType() {
    return AwsImportType.SQS;
  }

  @Override
  public void importService(ApplicationArguments args) {
    log.info("Migrating queues");
    var queueList = getQueueNames(sqsClient.listQueues(), args);

    queueList
        .forEach(
            queueName -> {
              try {
                log.info("Creating queue: " + queueName);
                localSqsClient.createQueue(
                    CreateQueueRequest.builder().queueName(queueName).build());
              } catch (Exception e) {
                log.error("An error occurred when creating the queue {}", queueName, e);
              }
            });
  }

  private List<String> getQueueNames(ListQueuesResponse listQueuesResponse,
      ApplicationArguments args) {
    var queueNames = listQueuesResponse.queueUrls().stream().map(queueUrl -> {
      var queueSplit = queueUrl.split("/");
      return queueSplit[queueSplit.length - 1];
    }).toList();
    return filter(queueNames, args);
  }

  private List<String> filter(List<String> queueNames, ApplicationArguments args) {
    if (args.containsOption(SQS_PREFIX)) {
      return queueNames.stream()
          .filter(queueName -> queueName.startsWith(args.getOptionValues(SQS_PREFIX).get(0)))
          .toList();
    }
    return queueNames;
  }

}
