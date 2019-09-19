# Subscription manager
- [Overview](#overview)
- [Requirements](#requirements)
- [Build](#build)
- [Run](#run)
- [Configuration](#configuration)
  - [Format](#format)
  - [Files](#files)

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
| `TRANSFORM_AWS_LAMBDA_NAME` | Yes         | *None*                 | `lambdaName`                         |
| `PUSHER_LAMBDA_NAME`        | Yes         | *None*                 | `lambdaName`                         |
| `S3_BUCKET_NAME`            | *See below* | *None*                 | `bucketName`                         |
| `S3_BUCKET_NAMES`           | *See below* | `["{S3_BUCKET_NAME}"]` | `["bucketName1","bucketName2"]`      |

- `IN_MIDDLEWARE_LAMBDA`: Lambda name for the middleware called on requests.
- `OUT_MIDDLEWARE_LAMBDA`: Lambda name for the middleware called on responses.
- `EVENT_STORE_SNS_TOPIC_ARN`: SNS Topic ARN where storage events will be sent for components like the subscription manager.
- `TRANSFORM_AWS_LAMBDA_NAME`: Lambda name for creating event views if a scope is present.
- `PUSHER_LAMBDA_NAME`: Lambda name for performing the push operation to the various targets.
- `S3_BUCKET_NAME`: Name of the S3 bucket where data is stored.
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
- SQS
  - `sqs:SendMessageBatch`
  - `sqs:SendMessage`
  
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

#### Publish processor
- Runtime: `Java 8`
- Handler: `LambdaPublishProcessor::handleRequest`
- Memory: 128MB minimum, 192MB recommended
- Timeout: 1 min

#### Notify processor
- Runtime: `Java 8`
- Handler: `LambdaNotifyProcessor::handleRequest`
- Memory: 128MB minimum, 192MB recommended
- Timeout: 1 min

#### Outbox processor
- Runtime: `Java 8`
- Handler: `LambdaOutboxProcessor::handleRequest`
- Memory: 128MB minimum, 192MB recommended
- Timeout: 1 min

#### Push processor
- Runtime: `Java 8`
- Handler: `LambdaPusherProcessor::handleRequest`
- Memory: 128MB minimum, 192MB recommended
- Timeout: 1 min

## Configuration
### Format
A subscription has the following structure:
```json
{
  "id": "<ID of the subscription>",
  "agent": "<IRI of who subscribed>",
  "object": "<URI of the resource to match>",
  "target": "<URI of where the notification should be sent>",
  "scope": {
    "smth": "smth"
  },
  "config": {
    "arbitraryKey": "arbitraryValue",
    "otherKey": "otherValue"
  },
  "needContext": false
}
```
`id` is optional and will be automatic generated at runtime. If set, it must be unique across the whole installation.
It is currently only used for logging purposes.

---

`agent` is the LinkedData IRI of the entity that subscribed and should received the notification.
It is optional if `target` is set. If not, the actual target of notifications will be auto-discovered using the inbox mechanism.

---

`object` is the URI to match on. This can have various format, depending on how the level where the subscriptions are stored.

Examples of valid URIs:
- `/inbox/`
- `https://*.pods.example.org/inbox/`

---

`target` is the actual endpoint to be used when sending notifications and will overwrite any discovery mechanism used for `agent`.

The following schemes are available:
- `http` (will use `POST`)
- `https` (will use `POST`)
- `aws-sqs` (If FIFO, must have content deduplication enabled)
- `aws-lambda`

Example of valid URIs:
- `https://example.org/path/to/receicinv/endpoint`
- `aws-sqs://sqs.us-east-2.amazonaws.com/123456789012/MyQueue`
- `aws-lambda://lambdaName`

---

`scope` is the scope that should be applied to this subscription.

---

---

`config` is an arbitrary object depending on the scheme used in the URI. See below for more information.

---

`needContext` is a basic filter to remove personal/confidential/credentials data from the raw store event that triggered
the subscription. It is `false` by default and will strip out those values.

### Schemes
#### HTTP
```json
{
  "sign": {
    "type": "sha1-salt",
    "salt": "<If application, salt value to hash the payload with>"
  },
  "headers": {
    "X-Header-Single-Value": [
      "value"
    ],
    "X-Header-Multi-Values": [
      "value1",
      "value2"
    ]
  }
}
```

##### Signatures

HTTP requests can be signed

The following signature methods are available:

- `sha1-salt`: Following Github's [signature method](https://developer.github.com/webhooks/securing/), this is a SHA-1 signature on the body content and a given salt, usually called "secret".

##### Headers

Arbitrary headers can be set. Header names are set has an object key, and the value is an array of strings, to be RFC compliant.

### Files

#### Pod-specific
Pod-specific subscriptions are stored at `/settings/subscriptions` and can be modified by the pod owner directly.

The file format is as follow:
```json
{
  "items": [
    {
      "id": "First subscription"
    },
    {
      "id": "Second subscription"
    }
  ]
}
```

Specific restrictions on those subscriptions:
- for `object`, only the URI path is matched and the rest is ignored as it is already scoped to the pod.

Example of a simple subscription that redirects all inbox events to a SQS queue:
```json
{
  "items": [
    {
      "object": "/inbox/",
      "target": "aws-sqs://sqs.us-east-2.amazonaws.com/123456789012/MyQueue"
    }
  ]
}
```
