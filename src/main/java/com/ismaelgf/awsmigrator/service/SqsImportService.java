package com.ismaelgf.awsmigrator.service;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

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
    var queueList = sqsClient.listQueues();

    queueList
        .queueUrls()
        .forEach(
            queueUrl -> {
              try{
                  var queueSplit = queueUrl.split("/");
                  var queueName = queueSplit[queueSplit.length - 1];
                  log.info("Creating queue: " + queueName);
                  localSqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
              } catch (Exception e) {
                  log.error("An error ocurred when creating the queue {}", queueUrl, e);
              }
            });
  }
}
