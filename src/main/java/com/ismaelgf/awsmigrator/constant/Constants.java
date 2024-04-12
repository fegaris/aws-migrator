package com.ismaelgf.awsmigrator.constant;

public class Constants {

    private Constants() {
    }

    public static final String LOCALHOST = "http://localhost:4566";
    public static final String LOCALSTACK_ACCOUNT_ID = "000000000000";

    /*
        Service names
         */
    public static final String SERVICE_NAME = "service-name";
    public static final String SERVICE_NAME_EVENT_BRIDGE = "eventbridge";
    public static final String SERVICE_NAME_SQS = "sqs";
    public static final String SERVICE_NAME_SFN = "sfn";
    public static final String SERVICE_NAME_LAMBDA = "lambda";
    public static final String SERVICE_NAME_DYNAMO_DB = "dynamodb";
    public static final String DYNAMO_DB_PREFIX = "dynamodb-prefix";
    public static final String DYNAMO_DB_IMPORT_DATA = "dynamodb-import-data";
    public static final String SFN_PREFIX = "sfn-prefix";
    /*
    Event bridge
     */
    public static final String DEFAULT = "default";
    public static final String EVENT_BUS_NAME = "event-bus-name";
    public static final String EVENT_BRIDGE_ENABLED_FILTER = "eventbridge-enabled";

    /*
    Lambda
     */
    public static final String LAMBDA_REPLACEMENT = "lambda-replacement";
    public static final String LAMBDA_PREFIX = "lambda-prefix";
    public static final String LAMBDA_ENVIRONMENT_VARIABLES = "lambda-variables";


    /*
    SQS
     */
    public static final String SQS_PREFIX_FILTER = "sqs-prefix";

}
