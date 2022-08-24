package com.ismaelgf.awsmigrator.service;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.FunctionCode;
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest;


@Slf4j
@AllArgsConstructor
@Service
public class LambdaImportService implements AwsImportService{


  @Qualifier("localLambdaClient")
  private final LambdaClient localLambdaClient;

  private final LambdaClient lambdaClient;

  @Override
  public AwsImportType getType() {
    return AwsImportType.LAMBDA;
  }

  @Override
  public void importService(ApplicationArguments args) {

    lambdaClient.listFunctions().functions().forEach(functionConfiguration -> {
      var originalLambda = lambdaClient.getFunction(GetFunctionRequest.builder()
          .functionName(functionConfiguration.functionName()).build());
//      localLambdaClient.createFunction(CreateFunctionRequest.builder()
//              .code(originalLambda.code())
//          .build());
    });



  }
}
