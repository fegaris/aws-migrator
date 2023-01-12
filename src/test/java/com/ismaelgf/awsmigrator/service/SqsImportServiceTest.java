package com.ismaelgf.awsmigrator.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ismaelgf.awsmigrator.constant.Constants;
import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;

@ExtendWith(SpringExtension.class)
class SqsImportServiceTest {

  @Mock
  @Qualifier("targetSqsClient")
  private SqsClient targetSqsClient;

  @Mock
  @Qualifier("sourceSqsClient")
  private SqsClient sourceSqsClient;


  private SqsImportService sqsImportService;

  @BeforeEach
  public void init() {
    sqsImportService = new SqsImportService(targetSqsClient, sourceSqsClient);
  }

  @Test
  void getType() {
    assertEquals(AwsImportType.SQS, sqsImportService.getType());
  }

  @Test
  void givenEmptyQueueListWhenImportThenDoesNotThrowAnyException() {
    var args = new DefaultApplicationArguments();
    when(sourceSqsClient.listQueues()).thenReturn(ListQueuesResponse.builder().build());
    assertDoesNotThrow(() -> sqsImportService.importService(args));
    verifyNoInteractions(targetSqsClient);
  }

  @Test
  void givenQueueListWhenImportThenCreateNewsQueueInTarget() {
    var args = new DefaultApplicationArguments();
    var listQueueResponse = createListQueuesResponse();
    when(sourceSqsClient.listQueues()).thenReturn(listQueueResponse);
    assertDoesNotThrow(() -> sqsImportService.importService(args));
    verify(targetSqsClient,
        times(listQueueResponse.queueUrls().size())).createQueue(any(CreateQueueRequest.class));
  }

  @Test
  void givenQueueListWhenImportWithFiltersThenCreateNewsQueueInTarget() {
    var args = new DefaultApplicationArguments("--" + Constants.SQS_PREFIX_FILTER + "=test");
    var listQueueResponse = createListQueuesResponse();
    when(sourceSqsClient.listQueues()).thenReturn(listQueueResponse);
    assertDoesNotThrow(() -> sqsImportService.importService(args));
    verify(targetSqsClient,
        times(1)).createQueue(any(CreateQueueRequest.class));
  }

  private ListQueuesResponse createListQueuesResponse() {
    return ListQueuesResponse.builder()
        .queueUrls("https://sqs.eu-west-1.amazonaws.com/1234567890/queue-test",
            "https://sqs.eu-west-1.amazonaws.com/1234567890/test-queue",
            "https://sqs.eu-west-1.amazonaws.com/1234567890/aws-migrator")
        .build();
  }
}