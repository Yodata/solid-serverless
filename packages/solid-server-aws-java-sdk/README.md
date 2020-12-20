# Solid Server on AWS - Java SDK
- [Overview](#overview)
- [Build](#build)
- [Use](#use)

## Overview

This SDK adds implementations and/or extensions of the Solid Server SDK for an AWS environment.    
It requires Java 8 or later.

## Build
### Binaries
To build a standalone jar usable in projects:
```bash
./gradlew build
```
Jar is produced in `build/libs/`


### Test
To run the tests:
```bash
./gradlew test
```
Detailed results are produced and saved at `build/reports/tests/test/index.html`

### Javadoc
To produce the javadoc:
```bash
./gradlew javadoc
```
Then go to `build/docs/javadoc/`

## Use
To use in other projects, either build the binary jar and include in your classpath, or import the project directly.

### Gradle
In `settings.gradle`:
```groovy
include(':aws-sdk')
project(':aws-sdk').projectDir = file('/path/to/this/sdk')

```
In `build.gradle`:
```groovy
dependencies {
    api project(':aws-sdk')
    
    // Other dependencies...
}
```

## Configuration
### Environment
When using this SDK, you must specify the `S3_BUCKET_NAME` environment variable so the `solid-serverless.json` config 
file can be found. This is the same value as the `s3.bucket.name` key listed below.

### solid-serverless.json
Base namespace is `aws`. All keys below are relative to this namespace:

Key                     | Purpose
------------------------|------------------------------------------------------
`s3.bucket.name`        | S3 bucket used for storage
`sns.event.store.topic` | SNS topic used to send Storage Events
`lambda.middleware.in`  | Name of the Lambda function used for processing requests on the way in
`lambda.middleware.out` | Name of the lambda function used for processing requests on the way out
`lambda.transform.name` | Name of the lambda function used to create views when a subscription with a scope is processed
`lambda.pusher.name`    | Name of the lambda function used to push notifications. This is only used if `sqs.pusher.url` is not set
`sqs.pusher.url`        | URL of the SQS queue where push notifications should be sent for async processing

Example:
```json
{
  "reflex": {
    "see core doc": "for details"
  },
  "aws": {
    "lambda": {
      "middleware": {
        "in": "solid-serverless-middleware-in",
        "out": "solid-serverless-middleware-out"
      },
      "transform": {
        "name": "solid-serverless-transform"
      },
      "pusher": {
        "name": "solid-serverless-pusher"
      }
    },
    "sqs": {
      "pusher": {
        "url": "https://sqs.us-east-1.amazonaws.com/123456789/solid-serverless-pusher"
      }
    },
    "s3": {
      "bucket": {
        "name": "myOrg-solid-serverless-storage"
      }
    },
    "sns": {
      "event": {
        "store": {
          "topic": "arn:aws:sns:us-east-1:123456789:solid-serverless-store-events"
        }
      }
    }
  }
}
```
