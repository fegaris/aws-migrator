package com.ismaelgf.awsmigrator.service;

import static com.ismaelgf.awsmigrator.constant.Constants.LAMBDA_ENVIRONMENT_VARIABLES;
import static com.ismaelgf.awsmigrator.constant.Constants.LAMBDA_PREFIX;
import static com.ismaelgf.awsmigrator.constant.Constants.LAMBDA_REPLACEMENT;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import com.ismaelgf.awsmigrator.service.model.LambdaReplacementType;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.DeleteFunctionRequest;
import software.amazon.awssdk.services.lambda.model.Environment;
import software.amazon.awssdk.services.lambda.model.FunctionCode;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest;
import software.amazon.awssdk.services.lambda.model.ListFunctionsRequest;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;


@Slf4j
@AllArgsConstructor
@Service
public class LambdaImportService implements AwsImportService {


  @Qualifier("localLambdaClient")
  private final LambdaClient localLambdaClient;

  private final LambdaClient lambdaClient;

  @Override
  public AwsImportType getType() {
    return AwsImportType.LAMBDA;
  }

  @Override
  public void importService(ApplicationArguments args) {
//    var lambdaReplacementType = getLambdaReplacementType(args);

    List<FunctionConfiguration> functionConfigurationList = getFunctions();
    filterLambdas(functionConfigurationList, args)
        .forEach(functionConfiguration -> {
          log.info("Migrating lambda: {}", functionConfiguration.functionArn());

//          if (lambdaReplacementType == LambdaReplacementType.NEW) {
          //TODO compare and remove first the lambdas to replace
          //TODO REPLACE is this and NEW remove all first
          deleteIfExists(functionConfiguration);
//          }

          try {
            var originalLambda = lambdaClient.getFunction(GetFunctionRequest.builder()
                .functionName(functionConfiguration.functionName()).build());

            var createLambdaResponse = localLambdaClient.createFunction(
                CreateFunctionRequest.builder()
                    .functionName(functionConfiguration.functionName())
                    .architectures(functionConfiguration.architectures())
                    .description(functionConfiguration.description())
                    .role(functionConfiguration.role())
                    .timeout(functionConfiguration.timeout())
                    .handler(functionConfiguration.handler())
                    .runtime(functionConfiguration.runtime())
                    .publish(true)
                    .code(FunctionCode.builder()
                        .zipFile(SdkBytes.fromInputStream(
                            new URL(originalLambda.code().location()).openStream())).build())
                    .environment(
                        Environment.builder().variables(getEnvironmentVariables(args)).build())
                    .build());
            log.info("Created lambda: {}", createLambdaResponse.functionArn());
          } catch (Exception e) {
            log.error("Error creating lambda: {}", functionConfiguration.functionArn(), e);
          }
        });


  }

  private List<FunctionConfiguration> getFunctions() {
    var functions = new ArrayList<FunctionConfiguration>();
    ListFunctionsResponse response = lambdaClient.listFunctions();
    functions.addAll(response.functions());
    while (Objects.nonNull(response.nextMarker())) {
      response = lambdaClient.listFunctions(ListFunctionsRequest.builder().marker(response.nextMarker()).build());
      functions.addAll(response.functions());
    }
    return functions;
  }

  private Map<String, String> getEnvironmentVariables(ApplicationArguments args) {
    var environmentVariables = new HashMap<String, String>();
    if (args.containsOption(LAMBDA_ENVIRONMENT_VARIABLES)) {
      args.getOptionValues(LAMBDA_ENVIRONMENT_VARIABLES)
          .forEach(environmentArg -> Arrays.stream(environmentArg.split(",")).toList()
              .forEach(envValues -> {
                var envKeyValue = envValues.split("->");
                environmentVariables.put(envKeyValue[0], envKeyValue[1]);
              }));
    }

    return environmentVariables;
  }

  private List<FunctionConfiguration> filterLambdas(List<FunctionConfiguration> list,
      ApplicationArguments args) {
    return list.stream().filter(functionConfiguration -> {
      if (args.containsOption(LAMBDA_PREFIX)) {
        return functionConfiguration.functionName()
            .startsWith(args.getOptionValues(LAMBDA_PREFIX).get(0));
      }
      return true;
    }).toList();
  }

  private void deleteIfExists(FunctionConfiguration functionConfiguration) {
    try {
      localLambdaClient.getFunction(GetFunctionRequest.builder()
          .functionName(functionConfiguration.functionName()).build());
      localLambdaClient.deleteFunction(DeleteFunctionRequest.builder().functionName(
          functionConfiguration.functionName()).build());
      log.info("Lambda deleted: {}", functionConfiguration.functionArn());
    } catch (ResourceNotFoundException resourceNotFoundException) {
      log.debug("Lambda not exists in local: {}", functionConfiguration.functionArn());
    }
  }

  private LambdaReplacementType getLambdaReplacementType(ApplicationArguments args) {
    if (args.containsOption(LAMBDA_REPLACEMENT)) {
      return LambdaReplacementType.getLambdaReplacementType(
          args.getOptionValues(LAMBDA_REPLACEMENT).get(0));
    }
    return LambdaReplacementType.NEW;
  }
}