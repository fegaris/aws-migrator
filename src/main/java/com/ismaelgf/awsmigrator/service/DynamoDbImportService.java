package com.ismaelgf.awsmigrator.service;

import static com.ismaelgf.awsmigrator.service.model.AwsImportType.DYNAMO_DB;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

@Slf4j
//@AllArgsConstructor
@Service
public class DynamoDbImportService implements AwsImportService {

  @Qualifier("localDynamoDbClient")
  @Autowired
  private DynamoDbClient localClient;

  @Qualifier("dynamoDbClient")
  @Autowired
  private DynamoDbClient client;


  @Override
  public AwsImportType getType() {
    return DYNAMO_DB;
  }

  @Override
  public void importService(ApplicationArguments args) {

//    client.listTables().tableNames().forEach(tableName -> {
    var tableName = "dev-mc-cfeng-CallbackOrderProcess";
    var describeTableResponse = client.describeTable(
        DescribeTableRequest.builder().tableName(tableName).build());

    localClient.createTable(CreateTableRequest.builder()
        .tableName(tableName)
        .keySchema(describeTableResponse.table().keySchema())
//            .globalSecondaryIndexes(describeTableResponse.table().globalSecondaryIndexes())
        .attributeDefinitions(describeTableResponse.table().attributeDefinitions())
        .provisionedThroughput(
            ProvisionedThroughput.builder().readCapacityUnits(1L).writeCapacityUnits(1L).build())
        .build());

//    });

  }
}
