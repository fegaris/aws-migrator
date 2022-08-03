package com.ismaelgf.awsmigrator.service.model;

import static com.ismaelgf.awsmigrator.constant.Constants.SERVICE_NAME_EVENT_BRIDGE;
import static com.ismaelgf.awsmigrator.constant.Constants.SERVICE_NAME_SQS;

public enum AwsImportType {
    EVENT_BRIDGE(SERVICE_NAME_EVENT_BRIDGE),
    SQS(SERVICE_NAME_SQS);


    private final String serviceName;
    AwsImportType(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }


}
