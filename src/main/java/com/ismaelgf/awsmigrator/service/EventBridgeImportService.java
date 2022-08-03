package com.ismaelgf.awsmigrator.service;

import static com.ismaelgf.awsmigrator.constant.Constants.DEFAULT;
import static com.ismaelgf.awsmigrator.constant.Constants.EVENT_BUS_NAME;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.CreateEventBusRequest;
import software.amazon.awssdk.services.eventbridge.model.ListEventBusesRequest;
import software.amazon.awssdk.services.eventbridge.model.ListRulesRequest;
import software.amazon.awssdk.services.eventbridge.model.ListTargetsByRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.ListTargetsByRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.PutTargetsRequest;
import software.amazon.awssdk.services.eventbridge.model.Rule;
import software.amazon.awssdk.services.eventbridge.model.Target;

@Slf4j
@AllArgsConstructor
@Service
public class EventBridgeImportService implements AwsImportService {
  @Qualifier("eventBridgeClient")
  private final EventBridgeClient eventBridgeClient;

  @Qualifier("localEventBridgeClient")
  private final EventBridgeClient localEventBridgeClient;

  @Override
  public AwsImportType getType() {
    return AwsImportType.EVENT_BRIDGE;
  }

  public void importService(final ApplicationArguments args) {
      getEventBusName(args).forEach(this::importEventBus);
  }

  private void importEventBus(String eventBusName) {
    createEventBus(eventBusName);
    var ruleMap = getRuleListMap(eventBusName);
    ruleMap.forEach(
            (rule, targets) -> {
                try{
                    createRuleAndTargets(eventBusName, rule, targets);
                } catch (Exception e) {
                    log.error("Error creating rule {}", rule.name(), e);
                }
            });
  }

  private void createRuleAndTargets(String eventBusName, Rule rule, List<Target> targets) {
        var putRuleRequest =
            PutRuleRequest.builder()
                .eventBusName(eventBusName)
                .name(rule.name())
                .description(rule.description())
                .eventPattern(rule.eventPattern())
                .roleArn(rule.roleArn())
                .build();
        var targetsRequest =
            PutTargetsRequest.builder()
                .eventBusName(eventBusName)
                .rule(rule.name())
                .targets(targets)
                .build();

        var ruleResponse = localEventBridgeClient.putRule(putRuleRequest);
        var targetsResponse = localEventBridgeClient.putTargets(targetsRequest);

        log.info(ruleResponse.ruleArn());
        log.info("Failed target entries: {}", targetsResponse.failedEntries().size());
    }

    private List<String> getEventBusName(ApplicationArguments args) {
    if (args.containsOption(EVENT_BUS_NAME)) {
      log.info("Migrating {} eventBus", args.containsOption(EVENT_BUS_NAME));
      args.getOptionValues(EVENT_BUS_NAME);
    }
    return List.of(DEFAULT);
  }

  private void createEventBus(String eventBusName) {
    if (localEventBridgeClient
        .listEventBuses(ListEventBusesRequest.builder().build())
        .eventBuses()
        .stream()
        .noneMatch(eventBus -> eventBus.name().equals(eventBusName))) {
      localEventBridgeClient.createEventBus(
          CreateEventBusRequest.builder().name(eventBusName).build());
    }
  }

  private Map<Rule, List<Target>> getRuleListMap(String eventBusName) {
    final Map<Rule, List<Target>> ruleMap = new HashMap<>();

    var response =
        eventBridgeClient.listRules(ListRulesRequest.builder().eventBusName(eventBusName).build());
    List<Rule> rules = response.rules();

    rules.forEach(
        rule -> {
          ListTargetsByRuleResponse listTargetsByRule =
              eventBridgeClient.listTargetsByRule(
                  ListTargetsByRuleRequest.builder()
                      .eventBusName(eventBusName)
                      .rule(rule.name())
                      .build());
          ruleMap.put(rule, listTargetsByRule.targets());

          log.info("Rule map loaded");
        });
    return ruleMap;
  }
}
