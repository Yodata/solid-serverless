# API - Front server
- [Overview](#overview)
- [Requirements](#requirements)
- [Build](#build)
- [Run](#run)

## Overview
This is the Front process for the Solid API, handling connections from clients and routing to middleware and backend services.

It integrates with custom backend stores and a link to middleware to process Data policies and scopes on requests/responses.

It comes built-in with AWS support in the form of Lambda call support for the middleware, and overall AWS structures as
it uses the [AWS SDK](../../packages/solid-server-aws-java-sdk/README.md).

## Requirements
- Java 8 or later

## Build
### Binaries
```bash
./gradlew shadowJar
```

### Docker
To build a docker image:
```bash
./gradlew dockerBuild
```
The `shadowJar` target is automatically called.

---

To tag a previous built image matching the repository version to `latest`, use:
```bash
./gradlew dockerTagLatest
```

---

If you would like to build an image and tag as latest, a convenient task is available:
```bash
./gradlew dockerBuildLatest
```

---

The image name will match the name of the project and its version will match the git-describe version.  
To provide custom name and versions, you can use the following project properties:
- `docker.image.name`: Set a specific image name
- `docker.image.version` Set a specific image version

Per example, to produce an image `org/customName:customVersion`:
```bash
./gradlew -Pdocker.image.name='org/customName' -Pdocker.image.version='customVersion' <task>
```

## Run
### Environment
The following environment variables are used:

| Name                        | Required    | Default value          | Example Value                        |
|-----------------------------|-------------|------------------------|--------------------------------------|
| `FRONTD_LOAD_MULTIPLIER`    | No          | `1`                    | `1`                                  |
| `IN_MIDDLEWARE_LAMBDA`      | Yes         | *None*                 | `lambdaName`                         |
| `OUT_MIDDLEWARE_LAMBDA`     | Yes         | *None*                 | `lambdaName`                         |
| `EVENT_STORE_SNS_TOPIC_ARN` | Yes         | *None*                 | `arn:aws:sns:region:12345:topicName` |
| `S3_BUCKET_NAME`            | *See below* | *None*                 | `bucketName`                         |
| `S3_BUCKET_NAMES`           | *See below* | `["{S3_BUCKET_NAME}"]` | `["bucketName1","bucketName2"]`      |

- `FRONTD_LOAD_MULTIPLIER` is an overall multiplier to configure what load to handle. This typically defines things like
  the amount of background threads, HTTP worker threads, etc.
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
```bash
MY_ENV_VAR=value java -jar build/libs/api-front.jar
```

### Docker
```bash
docker --rm -d -e "ENV_VARIBLE=value" -e "..." api-front
```
