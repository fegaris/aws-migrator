package com.ismaelgf.awsmigrator.service;

import static com.ismaelgf.awsmigrator.constant.Constants.SQS_PREFIX_FILTER;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsImportService implements AwsImportService {

  @Qualifier("targetSqsClient")
  private final SqsClient targetSqsClient;

  @Qualifier("sourceSqsClient")
  private final SqsClient sourceSqsClient;

  @Override
  public AwsImportType getType() {
    return AwsImportType.SQS;
  }

  @Override
  public void importService(ApplicationArguments args) {
    log.info("Migrating queues");
    var queueList = getQueueNames(sourceSqsClient.listQueues(), args);

    queueList
        .forEach(
            queueName -> {
              try {
                log.info("Creating queue: " + queueName);
                if (queueName.contains(".fifo")) {
                  targetSqsClient.createQueue(
                      CreateQueueRequest.builder().queueName(queueName).attributes(Map.of(
                          QueueAttributeName.FIFO_QUEUE, "true",
                          QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true")).build());
                } else {
                  targetSqsClient.createQueue(
                      CreateQueueRequest.builder().queueName(queueName).build());
                }
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
    if (args.containsOption(SQS_PREFIX_FILTER)) {
      return queueNames.stream()
          .filter(queueName -> queueName.startsWith(args.getOptionValues(SQS_PREFIX_FILTER).get(0)))
          .toList();
    }
    return queueNames;
  }

}
