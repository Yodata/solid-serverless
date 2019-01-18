# API Key Service
This service allows acts as the authoritative service to create, update and revoke API keys.

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

## Execute
Must be deployed as an AWS lambda and called from there.

> **TODO**: Add Lambda configuration

Entry point is `LambdaApiKeyService::handleRequest`

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

### Update
*To be implemented*

### Delete
*To be implemented*
