package com.ismaelgf.awsmigrator.constant;

public class Constants {

    private Constants() {
    }

    public static final String LOCALHOST = "http://localhost:4566";
    /*
        Service names
         */
    public static final String SERVICE_NAME = "service-name";
    public static final String SERVICE_NAME_EVENT_BRIDGE = "eventBridge";
    public static final String SERVICE_NAME_SQS = "sqs";
    public static final String SERVICE_NAME_SFN = "sfn";
    public static final String SERVICE_NAME_LAMBDA = "lambda";
    /*
    Event bridge
     */
    public static final String DEFAULT = "default";
    public static final String EVENT_BUS_NAME = "event-bus-name";

    /*
    Lambda
     */
    public static final String LAMBDA_REPLACEMENT = "lambda-replacement";
    public static final String LAMBDA_PREFIX = "lambda-prefix";


}
