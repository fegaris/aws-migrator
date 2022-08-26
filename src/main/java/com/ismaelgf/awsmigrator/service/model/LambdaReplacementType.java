package com.ismaelgf.awsmigrator.service.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum LambdaReplacementType {
  NEW("new"),
  REPLACE("replace"),
  ONLY_REPLACE("only-replace");

  private static final Map<String, LambdaReplacementType> REPLACEMENT_MAP = new HashMap<>();
  private final String replacementType;

  static {
    Arrays.stream(LambdaReplacementType.values())
        .forEach(lambdaReplacementType ->
            REPLACEMENT_MAP.put(lambdaReplacementType.getReplacementType(), lambdaReplacementType));
  }

  LambdaReplacementType(String replacementType) {
    this.replacementType = replacementType;
  }

  public String getReplacementType() {
    return replacementType;
  }

  public static LambdaReplacementType getLambdaReplacementType(String replacementType) {
    return REPLACEMENT_MAP.getOrDefault(replacementType, NEW);
  }
}
