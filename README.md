# aws-migrator

Migrate your aws configuration to your localstack

# Services

Services that you can migrate to your local machine
You only can migrate a service for each execution
You can migrate multiples services concatenating service-name:

'--service-name=service1 .... --service-name=service2 ....'

**Params**

| Name         | Description                                             | Values      | Mandatory |
|--------------|---------------------------------------------------------|-------------|-----------|
| service-name | This is the name of the service that we want to migrate | eventBridge | TRUE      |   

## EventBridge

The service-name is **eventBridge**

| Name                | Mandatory | Description                                                    | Values   |
|---------------------|-----------|----------------------------------------------------------------|----------|
| event-bus-name      | No        | The name of the bus to migrate. The default value is "default" | default  |
| eventbridge-enabled | No        | Import only enabled rules. By default import all rules         | -------- |

Example:
java -jar aws-migrator.jar --service-name=eventBridge --event-bus-name=default

## Simple Queue Service (SQS)

The service name is **sqs**

| Name       | Mandatory | Description                            | Values  |
|------------|-----------|----------------------------------------|---------|
| sqs-prefix | No        | Import queues that matching the prefix | default |

Example:
java -jar aws-migrator.jar --service-name=sqs

## Lambda
//TODO in progress

# Requirements

To use this application you need:
- Localstack
- Java 17
- AWS credentials configured
