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

| Name           | Mandatory | Description                                                    | Values  |
|----------------|-----------|----------------------------------------------------------------|---------|
| event-bus-name | No        | The name of the bus to migrate. The default value is "default" | default |

Example:
java -jar aws-migrator.jar --service-name=eventBridge --event-bus-name=default

## Simple Queue Service (SQS)

The service name is **sqs**

Example:
java -jar aws-migrator.jar --service-name=sqs


# Requeriments

To use this application you need:
- Java 17
