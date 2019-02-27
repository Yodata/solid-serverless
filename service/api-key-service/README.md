# API Key Service
This service allows acts as the authoritative gateway to create, update and revoke API keys.

## Requirements
- Java 8
- AWS Lambda

## Build
```bash
./gradlew shadowJar
```

## Configure
Via environment variables:

| Key Name                    | Required | Default | Example                                |
|-----------------------------|----------|---------|----------------------------------------|
| `EVENT_STORE_SNS_TOPIC_ARN` | **Yes**  | *None*  | `arn:aws:sns:region:123456:topic-name` |
| `S3_BUCKET_NAME`            | **Yes**  | *None*  | `s3BucketName`                         |

## Deploy to AWS

### First local build

Follow the [Build](#build) instructions.

### Create Lambda

In wizard:

- Name: `solid-server-api-key-service`
- Runtime: `Java 8`
- Role: Existing
- Existing Role: `solid-server-api-key-service`

In Lambda view:

- Function code

  - Code entry type: Upload .zip or .jar
  - Function package: Select `repo:/service/api-key-service/build/libs/api-key-service.jar`
  - Handler: `LambdaApiKeyService::handleRequest`

- Environment variables

  | Name                        | Value                                        |
  | --------------------------- | -------------------------------------------- |
  | `EVENT_STORE_SNS_TOPIC_ARN` | ARN of SNS topic `solid-server-store-events` |
  | `S3_BUCKET_NAME`            | `solid-server-storage`                       |

- Basic Settings

  - Memory: 192 MB
  - Timeout: `1` min `0`sec

### Push

```bash
make push
```

## Usage
### Create
Input is a JSON object of the following example format:
```json
{
  "type": "CreateAction",
  "object": {
    "agent": "https://<Pod hostname>/profile/card#me",
    "instrument": "https://<Pod Hostname>/profile/card#me"
  }
}
```
- `type`: *Required* - Must be `CreateAction`
- `object`: *Required*
  - `agent`: *Optional* - IRI of the agent
  - `instrument`: *Required* - IRI of the instrument

---

Output is a JSON object of the following format:
```json
{
  "id": "abcdefghij123456"
}
```
- `id`: ID of the key which was created.

### Update
*To be implemented*

### Delete
*To be implemented*
