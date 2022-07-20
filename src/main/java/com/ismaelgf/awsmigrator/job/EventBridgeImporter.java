package com.ismaelgf.awsmigrator.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Component
public class EventBridgeImporter {

  @Qualifier("eventBridgeClient")
  private final EventBridgeClient eventBridgeClient;

  @Qualifier("localEventBridgeClient")
  private final EventBridgeClient localEventBridgeClient;

  public void updateLocalEventBridge(String eventBusName) {
    final Map<Rule, List<Target>> ruleMap = getRuleListMap(eventBusName);

    ruleMap.keySet();

    if (localEventBridgeClient
        .listEventBuses(ListEventBusesRequest.builder().build())
        .eventBuses()
        .stream()
        .noneMatch(eventBus -> eventBus.name().equals(eventBusName))) {
      localEventBridgeClient.createEventBus(
          CreateEventBusRequest.builder().name(eventBusName).build());
    }

    ruleMap.forEach(
        (rule, targets) -> {
          PutRuleRequest putRuleRequest =
              PutRuleRequest.builder()
                  .eventBusName(eventBusName)
                  .name(rule.name())
                  .description(rule.description())
                  .eventPattern(rule.eventPattern())
                  .roleArn(rule.roleArn())
                  .build();
          PutTargetsRequest targetsRequest =
              PutTargetsRequest.builder().eventBusName(eventBusName).rule(rule.name()).targets(targets).build();

            PutRuleResponse ruleResponse = localEventBridgeClient.putRule(putRuleRequest);
            PutTargetsResponse targetsResponse = localEventBridgeClient.putTargets(targetsRequest);

            log.info(ruleResponse.ruleArn());
            log.info("Failed target entries: {}", targetsResponse.failedEntries().size());

        });
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

  private void listEventBuses() {
    ListEventBusesResponse response =
        eventBridgeClient.listEventBuses(ListEventBusesRequest.builder().limit(10).build());
    List<EventBus> buses = response.eventBuses();
  }
}
