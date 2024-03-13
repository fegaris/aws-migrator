package com.ismaelgf.awsmigrator.service;

import static com.ismaelgf.awsmigrator.constant.Constants.DEFAULT;
import static com.ismaelgf.awsmigrator.constant.Constants.EVENT_BRIDGE_ENABLED_FILTER;
import static com.ismaelgf.awsmigrator.constant.Constants.EVENT_BUS_NAME;
import static com.ismaelgf.awsmigrator.constant.Constants.LOCAL_ACCOUNT_ID;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
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
import software.amazon.awssdk.services.eventbridge.model.RuleState;
import software.amazon.awssdk.services.eventbridge.model.Target;

@Slf4j
@RequiredArgsConstructor
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
    getEventBusName(args).forEach(eventBusName -> importEventBus(eventBusName, args));
  }

  private void importEventBus(String eventBusName, ApplicationArguments args) {
    createEventBus(eventBusName);
    var ruleMap = getRuleListMap(eventBusName, args);
    ruleMap.forEach(
        (rule, targets) -> {
          try {
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
            .state(rule.state())
            .build();
    List<Target> newTargets = new ArrayList<>();
    targets.forEach(
        target ->
            newTargets.add(
                Target.builder()
                    .id(target.id())
                    .arn(replaceLocalAccount(target.arn()))
                    .deadLetterConfig(target.deadLetterConfig())
                    .build()));
    var targetsRequest =
        PutTargetsRequest.builder()
            .eventBusName(eventBusName)
            .rule(rule.name())
            .targets(newTargets)
            .build();

    var ruleResponse = localEventBridgeClient.putRule(putRuleRequest);
    var targetsResponse = localEventBridgeClient.putTargets(targetsRequest);

    log.info(ruleResponse.ruleArn());
    log.info("Status: {}", rule.state());
    log.info("Pattern: {}", rule.eventPattern());
    targets.forEach(target -> log.info("Target: {}", target.arn()));
    log.info("Failed target entries: {}", targetsResponse.failedEntries().size());
  }

  private String replaceLocalAccount(String arn) {
    Pattern pattern = Pattern.compile("(?<=:)(\\d+)(?=:)");
    Matcher matcher = pattern.matcher(arn);

    return matcher.replaceAll(LOCAL_ACCOUNT_ID);
  }

  private List<String> getEventBusName(ApplicationArguments args) {
    if (args.containsOption(EVENT_BUS_NAME)) {
      log.info("Migrating {} eventBus", args.containsOption(EVENT_BUS_NAME));
      return Arrays.asList(args.getOptionValues(EVENT_BUS_NAME).get(0).split(","));
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

  private Map<Rule, List<Target>> getRuleListMap(String eventBusName, ApplicationArguments args) {
    final Map<Rule, List<Target>> ruleMap = new HashMap<>();

    var response =
        eventBridgeClient.listRules(ListRulesRequest.builder().eventBusName(eventBusName).build());
    List<Rule> rules = filter(response.rules(), args);

    rules.forEach(
        rule -> {
          ListTargetsByRuleResponse listTargetsByRule =
              eventBridgeClient.listTargetsByRule(
                  ListTargetsByRuleRequest.builder()
                      .eventBusName(eventBusName)
                      .rule(rule.name())
                      .build());
          ruleMap.put(rule, listTargetsByRule.targets());
        });
    log.info("Rule map loaded");
    return ruleMap;
  }

  private List<Rule> filter(List<Rule> rules, ApplicationArguments args) {
    if (args.containsOption(EVENT_BRIDGE_ENABLED_FILTER)) {
      log.info("Filter enabled rules");
      return rules.stream().filter(rule -> RuleState.ENABLED == rule.state()).toList();
    }
    return rules;
  }
}
