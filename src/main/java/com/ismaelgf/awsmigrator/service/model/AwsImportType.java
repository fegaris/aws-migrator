package com.ismaelgf.awsmigrator.service.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.ismaelgf.awsmigrator.constant.Constants.SERVICE_NAME_EVENT_BRIDGE;
import static com.ismaelgf.awsmigrator.constant.Constants.SERVICE_NAME_SQS;

public enum AwsImportType {
    EVENT_BRIDGE(SERVICE_NAME_EVENT_BRIDGE),
    SQS(SERVICE_NAME_SQS);

    private final String serviceName;
    private static final Map<String, AwsImportType> typeMap = new HashMap<>();

    static {
    Arrays.stream(AwsImportType.values())
        .forEach(awsImportType -> typeMap.put(awsImportType.getServiceName(), awsImportType));
    }
    AwsImportType(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public AwsImportType getByServiceName(String serviceName) {
        return typeMap.get(serviceName);
    }

}
