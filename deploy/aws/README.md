# Solid Serverless

This guide is a step-by-step allowing to manually setup a fully working YoData Solid Serverless stack in AWS.

This guide will use `example.org` as "Base Domain" and other names with a placeholder intent.

Any path starting with `repo:/` indicates paths relative to the folder where this repository is stored.
Example: If the repository was cloned at `/home/user/repo` and for `repo:/src/`, the actual path would be `/home/user/repo/src/`.

The reader is expected to:

- Adapt the various names/labels to whatever naming policy is used in your environment.
- Use the same names for the same entities or their references while following this guide.
- Replace hostnames accordingly to their environment. Only wildcards represented by `*` are expected to remain.

This guide will only give instructions and/or values for relevant fields/options. If an available option is not documented, its default value should not be changed. If a value is mandatory, it is left to the reader discretion.

## Requirements

Requirements not directly related to the stack, like TLS certificates for the Load-balancer are considered out of scope as they directly depend on the target infrastructure itself rather than being specific to this stack.

The following is expected to be set up and/or available on your local machine before following this guide:

- A working developer docker environment, from the [Developer guide](../../dev.md).  
  **All command line commands listed in this guide must run in that environment**.

The following is expected to be set up and/or available in AWS before following this guide:

- A VPC for the various services. This can be the default or a dedicated one and is left at the discretion of the reader.
- A valid TLS certificate in ACM with subject names:
  - `example.org`
  - `*.example.org`
- AWS default policies:
  - `AmazonS3ReadOnlyAccess`
  - `AmazonSQSReadOnlyAccess`
  - `AWSLambdaBasicExecutionRole`
  - `AWSLambdaSQSQueueExecutionRole`

## IAM

The following steps happen in the IAM view.

### Policies

This section describes new policies to be created.

For each section:

- Section name is the suggested policy name
- First bullet point level is the AWS service
- Second bullet point level are the actions to be allowed on the AWS service

#### LambdaInvokeOnly

- Lamba
  - `InvokeFunction`
  - `InvokeAsync`

#### SQSSendMessageOnly
- SQS
  - `SendMessage`
  - `SendMessageBatch`

#### SNSPublishOnly
- SNS
  - `Publish`

#### S3WriteOnly

- S3
  - `DeleteObject`
  - `PutObject`

### Roles

The following new roles are to be created:

#### Front API ECS task

- Service: Elastic Container Service
- Use case: Elastic Container Service Task
- Policies to be applied:
  - `LambdaInvokeOnly`
  - `SNSPublishOnly`
  - `AmazonS3ReadOnlyAccess`
  - `S3WriteOnly`
- Name: `solid-server-api-front`

#### Store events Lambda processor

- Service: Lambda
- Use case: Lambda
- Policies to be applied:
  - `AWSLambdaBasicExecutionRole`
  - `AmazonS3ReadOnlyAccess`
  - `AmazonSQSReadOnlyAccess`
  - `LambdaInvokeOnly`
  - `S3WriteOnlyAccess`
  - `SNSPublishOnly`
  - `SQSSendMessageOnly`
- Name: `solid-server-store-event-processor`

#### Inbox Lambda processor

- Service: Lambda
- Use case: Lambda
- Policies to be applied:
  - `AmazonS3ReadOnlyAccess`
  - `AWSLambdaSQSQueueExecutionRole`
  - `LambdaInvokeOnly`
  - `S3WriteOnlyAccess`
  - `SNSPublishOnly`
- Name: `solid-server-inbox-processor`

#### Outbox Lambda processor

- Service: Lambda
- Use case: Lambda
- Policies to be applied:
  - `AmazonS3ReadOnlyAccess`
  - `AWSLambdaSQSQueueExecutionRole`
  - `LambdaInvokeOnly`
  - `S3WriteOnlyAccess`
  - `SNSPublishOnly`
- Name: `solid-server-outbox-processor`

#### Push Processor

- Service: Lambda
- Use case: Lambda
- Policies to be applied:
  - `AmazonS3ReadOnlyAccess`
  - `AWSLambdaSQSQueueExecutionRole`
  - `LambdaInvokeOnly`
  - `S3WriteOnlyAccess`
  - `SNSPublishOnly`
- Name: `solid-server-push-processor`

## SNS
Create a new topic for the store events, consumed by the Subscription Manager:

- Topic name: `solid-server-store-events`

## SQS
Create the following queues:

#### Inbox processing

- Name: `solid-server-inbox-store-events`
- Type: Standard Queue
- Configuration
  - Queue Attributes
    - Default Visibility Timeout: 60 seconds
    - Message Retention Period: 14 days
    - Maximum Message Size: *Max value*

#### Outbox processing

- Name: `solid-server-outbox-store-events`
- Type: Standard Queue
- Configuration
  - Queue Attributes
    - Default Visibility Timeout: 60 seconds
    - Message Retention Period: 14 days
    - Maximum Message Size: *Max value*

## S3

Create a single S3 bucket to hold configuration and data for the server:

- Name: `solid-server-storage`

Afterwards, perform the following actions using `repo:/deploy/aws/s3/` as your working directory:

- Edit `internal/subscriptions` and set the SQS queue URLs to those created above and replace the leading `https://` by `aws-sqs://`. The file contains sample values as example.
- Copy the directory and file structure from the working directory to the S3 bucket, per example using `aws s3 cp [options]`
- Remove `global/security/api/key/.gitignore` from the S3 bucket.

## EC2
### Key Pairs

Create a new Key pair or re-use an appropriate one for your environment. It will be used for the EC2 instances used in the ECS setup for the API Front process.

### Security Groups

On top of the default VPC security group, the following security groups are to be created

#### Front API Load Balancer

- Name: `solid-server-api-front-lb`
- VPC: *Default*
- Inbound
  - `HTTPS` type with `Anywhere` source (`0.0.0.0/0, ::/0`)

#### Front API ECS tasks

- Name: `solid-server-api-front`
- VPC: *Default*
- Inbound
  - `All traffic` from itself
  - `All TCP` from the Security Group `solid-server-api-front-lb`

### Load Balancers

Create a new **Application Load Balancer** using the following setup that will be used in front of the Front API ECS tasks:

#### Step 1
##### Basic
- Name: `solid-server-api-front`
- Scheme: Internet-facing
##### Listeners

Have a single listener:

- Protocol: HTTPS
- Port: `443`

##### Availability zones
All within the relevant VPC.

#### Step 2
##### Default cert
Pick the appropriate TLS certificate for your infrastructure.

##### Security Policy
`ELBSecurityPolicy-TLS-1-2-Ext-2018-06`

#### Step 3
- Security group: `solid-server-api-front-lb`

#### Step 4
##### Target group
- Name: `solid-server-api-front-tg`
- Target: Instance
- Protocol: HTTP
- Port: `9000`

###### Health Checks
- Protocol: HTTP
- Path: `/status`
- Advanced settings
  - Port: Traffic port
  - Healthy threshold: `2`
  - Unhealthy threshold: `2`
  - Timeout: `5`
  - Interval: `10`
  - Success codes: `200`

#### Step 5
Skip this step.

After the wizard, edit the **Deregistration delay** and set it to `60` seconds.

## Lambda

### Middleware

For each directory in the following list:

- `repo:/service/api-middleware`
- `repo:/service/check-scope`
- `repo:/service/create-view`
- `repo:/service/data-policy`
- `repo:/service/data-processing`
- `repo:/service/echo-service`
- `repo:/service/validate-schema`

For the directory as your current working directory, run the following command in your Docker Dev Env:

```bash
make push
```

### Subscription Manager

Build the [Subscription Manager](../../service/subscription-manager/README.md) service then create the following lambdas functions from scratch:

#### Store event processor

In wizard:

- Name: `solid-server-store-event-processor`
- Runtime: `Java 8`
- Role: Existing
- Existing Role: `solid-server-store-event-processor`

In Lambda view:

- Add an enabled SNS trigger with the ARN of `solid-server-store-events`

- Function code

  - Code entry type: Upload .zip or .jar
  - Function package: Select `repo:/service/subscription-manager/build/libs/subscription-manager.jar`
  - Handler: `LambdaStoreEventProcessor::handleRequest`

- Environment variables

  | Name                        | Value                                        |
  | --------------------------- | -------------------------------------------- |
  | `IN_MIDDLEWARE_LAMBDA`      | `api-middleware`                             |
  | `OUT_MIDDLEWARE_LAMBDA`     | `api-middleware`                             |
  | `PUSHER_LAMBDA_NAME`        | `solid-server-push-processor`                |
  | `EVENT_STORE_SNS_TOPIC_ARN` | ARN of SNS topic `solid-server-store-events` |
  | `S3_BUCKET_NAME`            | `solid-server-storage`                       |

- Basic Settings

  - Memory: 192 MB
  - Timeout: `1` min `0`sec

#### Inbox processor

In wizard:

- Name: `solid-server-inbox-processor`
- Runtime: `Java 8`
- Role: Existing
- Existing Role: `solid-server-inbox-processor`

In Lambda view:

- Add an enabled SQS trigger

  - SQS queue: `solid-server-inbox-store-events`
  - Batch size: `1`
  - Enabled: Yes

- Function code

  - Code entry type: Upload .zip or .jar
  - Function package: Select `repo:/service/subscription-manager/build/libs/subscription-manager.jar`
  - Handler: `LambdaInboxProcessor::handleRequest`

- Environment variables

  | Name                        | Value                                        |
  | --------------------------- | -------------------------------------------- |
  | `IN_MIDDLEWARE_LAMBDA`      | `solid-server-api-middleware`                |
  | `OUT_MIDDLEWARE_LAMBDA`     | `solid-server-api-middleware`                |
  | `EVENT_STORE_SNS_TOPIC_ARN` | ARN of SNS topic `solid-server-store-events` |
  | `S3_BUCKET_NAME`            | `solid-server-storage`                       |

- Basic Settings

  - Memory: 192 MB
  - Timeout: `1` min `0`sec

#### Outbox processor

- - In wizard:

    - Name: `solid-server-outbox-processor`
    - Runtime: `Java 8`
    - Role: Existing
    - Existing Role: `solid-server-outbox-processor`

    In Lambda view:

    - Add an enabled SQS trigger

      - SQS queue: `solid-server-outbox-store-events`
      - Batch size: `1`
      - Enabled: Yes

    - Function code

      - Code entry type: Upload .zip or .jar
      - Function package: Select `repo:/service/subscription-manager/build/libs/subscription-manager.jar`
      - Handler: `LambdaOutboxProcessor::handleRequest`

    - Environment variables

      | Name                        | Value                                        |
      | --------------------------- | -------------------------------------------- |
      | `IN_MIDDLEWARE_LAMBDA`      | `solid-server-api-middleware`                |
      | `OUT_MIDDLEWARE_LAMBDA`     | `solid-server-api-middleware`                |
      | `EVENT_STORE_SNS_TOPIC_ARN` | ARN of SNS topic `solid-server-store-events` |
      | `S3_BUCKET_NAME`            | `solid-server-storage`                       |

    - Basic Settings

      - Memory: 192 MB
      - Timeout: `1` min `0`sec

#### Push processor

In wizard:

- Name: `solid-server-push-processor`
- Runtime: `Java 8`
- Role: Existing
- Existing Role: `solid-server-push-processor`

In Lambda view:

- Function code
  - Code entry type: Upload .zip or .jar
  - Function package: Select `repo:/service/subscription-manager/build/libs/subscription-manager.jar`
  - Handler: `LambdaPusherProcessor::handleRequest`
- Environment variables
  - *No environment variable needed*
- Basic Settings
  - Memory: 192 MB
  - Timeout: `1` min `0`sec

## ECR

### Front API

- Create a new repository: `solid-server-api-front`

- Build and deploy the image: with `repo:/service/api-front` as your working directory in the Docker Dev env, run:

```bash
make push
```

- The newly pushed image will appear in the images of the ECR repository; Make a note of the Image URI.

## ECS

### Task Definitions

Create new Task Definition:

- Launch type: EC2

- Name: `solid-server-api-front`

- Task role: `solid-server-api-front`

- Container:
  - Name: `main`

  - Image: *Your ECR image*

  - Memory Limits

    - Type: Soft
    - Amount: `128`

  - Port mapping:

    - Host: `0`
    - Container: `9000`
    - Protocol: tcp

  - Environment

    - Variables

    | Name                        | Value                                        |
    | --------------------------- | -------------------------------------------- |
    | `IN_MIDDLEWARE_LAMBDA`      | `solid-server-api-middleware`                |
    | `OUT_MIDDLEWARE_LAMBDA`     | `solid-server-api-middleware`                |
    | `EVENT_STORE_SNS_TOPIC_ARN` | ARN of SNS topic `solid-server-store-events` |
    | `S3_BUCKET_NAME`            | `solid-server-storage`                       |

  - Storage and Logging

    - Log configuration: *Left to the reader discretion. In doubt, auto-configure is recommended.*

### Clusters

Create a new cluster:

- Template: EC2 Linux + Networking
- Name: `reflex-prod-solid-front`
- Instance configuration
  - Provision Model: On-Demand
  - EC2 Instance type: t2.micro
  - Number of instances: `2`
  - Key Pair: Your chosen one
- Networking
  - VPC: Your chosen one
  - Subnets: Your chosen ones
  - Security group: `solid-server-api-front`

### Services

On the cluster `reflex-prod-solid-front`, create a new service:

- Configure Service
  - Launch type: EC2
  - Task Definition
    - Family: `solid-server-api-front`
    - Revision: *latest*
  - Service name: `frontd`
  - Service type: REPLICA
  - Number of tasks: `2`
  - Minimum healthy percent: `50`
  - Maximum percent: `200`
- Deployments
  - Type: Rolling update
- Task Placement
  - Template: AZ Balanced Spread
- Load balancing
  - Type: Application Load Balancer
  - Name: `solid-server-api-front`
- Container to load balance
  - Add the `main:0:9000` to the load balancer
    - Listener port: *Pick 443:HTTPS*
    - Target group name: Pick `solid-server-api-front-tg`
- Service discovery: Do not use
- Auto Scaling: *Left to the reader discretion*