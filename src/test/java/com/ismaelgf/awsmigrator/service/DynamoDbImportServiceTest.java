package com.ismaelgf.awsmigrator.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class DynamoDbImportServiceTest {

//  @Autowired
  private DynamoDbImportService dynamoDbImportService;

//  @Test
  void importService() {
    dynamoDbImportService.importService(null);
  }
}