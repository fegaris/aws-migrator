package com.ismaelgf.awsmigrator.service;

import static com.ismaelgf.awsmigrator.constant.Constants.DYNAMO_DB_PREFIX;
import static com.ismaelgf.awsmigrator.service.model.AwsImportType.DYNAMO_DB;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

@Slf4j
@RequiredArgsConstructor
@Service
public class DynamoDbImportService implements AwsImportService {

  @Qualifier("localDynamoDbClient")
  private final DynamoDbClient localClient;

  @Qualifier("dynamoDbClient")
  private final DynamoDbClient client;


  @Override
  public AwsImportType getType() {
    return DYNAMO_DB;
  }

  @Override
  public void importService(ApplicationArguments args) {

    getTableNames(args).forEach(this::importTable);

  }

  private void importTable(String tableName) {
    try {
      log.info("Importing table: {}", tableName);
      var describeTableResponse = client.describeTable(
          DescribeTableRequest.builder().tableName(tableName).build());

      var provisionedThroughput = ProvisionedThroughput.builder().readCapacityUnits(1L)
          .writeCapacityUnits(1L).build();

      var gsi = describeTableResponse.table().globalSecondaryIndexes().stream().map(
          globalSecondaryIndexDescription -> GlobalSecondaryIndex.builder()
              .indexName(globalSecondaryIndexDescription.indexName())
              .keySchema(globalSecondaryIndexDescription.keySchema())
              .projection(globalSecondaryIndexDescription.projection())
              .provisionedThroughput(provisionedThroughput)
              .build()).toList();

      localClient.createTable(CreateTableRequest.builder().tableName(tableName)
          .keySchema(describeTableResponse.table().keySchema())
          .globalSecondaryIndexes(gsi.isEmpty() ? null : gsi)
          .attributeDefinitions(describeTableResponse.table().attributeDefinitions())
          .provisionedThroughput(provisionedThroughput)
          .build());
    } catch (Exception e) {
      log.error("Error importing table {}", tableName, e);
    }
  }

  public List<String> getTableNames(ApplicationArguments args) {
    if (args.containsOption(DYNAMO_DB_PREFIX)) {
      var prefix = args.getOptionValues(DYNAMO_DB_PREFIX).get(0);
      return client.listTables(
              ListTablesRequest.builder().limit(100).exclusiveStartTableName(prefix).build())
          .tableNames().stream().filter(s -> s.startsWith(prefix)).toList();
    } else {
      return client.listTables(ListTablesRequest.builder().limit(100).build()).tableNames();
    }
  }
}
