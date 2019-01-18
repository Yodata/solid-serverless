# Subscription manager
- [Overview](#overview)
- [Requirements](#requirements)
- [Build](#build)
- [Run](#run)

## Overview
This is the Subscription manager for the Solid server, handling storage events, processing the inbox and pushing to subscribers.  
It processes store events coming from various sources, like the API Front process.

It is made of three components:
- Store event processor: Handles the raw events and calls subscribers
- Inbox processor: Listens and process Inbox messages (ACL edit, subscription management, data transform, etc.)
- Outbox processor: Listens for new outbox messages to perform the actual pushing step

It comes built-in with AWS support in the form of three Lambdas entry points, one for each component.

## Requirements
- Java 8 or later

## Build
### Binaries
```bash
./gradlew shadowJar
```

## Run
### Environment
The following environment variables are used:

| Name                        | Required    | Default value          | Example Value                        |
|-----------------------------|-------------|------------------------|--------------------------------------|
| `IN_MIDDLEWARE_LAMBDA`      | Yes         | *None*                 | `lambdaName`                         |
| `OUT_MIDDLEWARE_LAMBDA`     | Yes         | *None*                 | `lambdaName`                         |
| `EVENT_STORE_SNS_TOPIC_ARN` | Yes         | *None*                 | `arn:aws:sns:region:12345:topicName` |
| `S3_BUCKET_NAME`            | *See below* | *None*                 | `bucketName`                         |
| `S3_BUCKET_NAMES`           | *See below* | `["{S3_BUCKET_NAME}"]` | `["bucketName1","bucketName2"]`      |

- `IN_MIDDLEWARE_LAMBDA`: Lambda name for the middleware called on requests
- `OUT_MIDDLEWARE_LAMBDA`: Lambda name for the middleware called on responses
- `EVENT_STORE_SNS_TOPIC_ARN`: SNS Topic ARN where storage events will be sent for components like the subscription manager
- `S3_BUCKET_NAME`: Name of the S3 bucket where data is stored
- `S3_BUCKET_NAMES`: If the data need to be replicated in several S3 buckets, this accepts a JSON array of S3 bucket names.
  The first bucket in the array will be considered as the main one and used for read operations.

`S3_BUCKET_NAMES` is set by default to an array with a single value, taken from `S3_BUCKET_NAME`. If you do not set
`S3_BUCKET_NAMES`, `S3_BUCKET_NAME` is required.

By default, AWS components are used. If you are not running in AWS, ensure the required environment/configuration for
AWS SDK/CLI are set, like `AWS_PROFILE` and `AWS_REGION`.

### From jar
There are no `main()` entry point. The components can only run as AWS Lambdas.

## Deploy

### AWS

Required permissions for each component:
- Lambda
  - `lambda:InvokeFunction`
  - `lambda:InvokeAsync`
- S3
  - `s3:Get*`
  - `s3:List*`
  - `s3:PutObject`
  - `s3:DeleteObject`
- SNS
  - `sns:Publish`
  
#### Store event processor
- Runtime: `Java 8`
- Handler: `LambdaStoreEventProcessor::handleRequest`
- Memory: 128MB minimum, 192MB recommended
- Timeout: 1 min

#### Inbox processor
- Runtime: `Java 8`
- Handler: `LambdaInboxProcessor::handleRequest`
- Memory: 128MB minimum, 192MB recommended
- Timeout: 1 min

#### Outbox processor
- Runtime: `Java 8`
- Handler: `LambdaOutboxProcessor::handleRequest`
- Memory: 128MB minimum, 192MB recommended
- Timeout: 1 min
