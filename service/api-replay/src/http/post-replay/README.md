# replay-api:post/replay

1. Receives a replay-request object,
2. validates input values,
3. transforms url to s3 url and iso date to s3 bucket prefix, and
4. sends it to the replay-start queue
5. returns action with actionStatus

## input parameters
```json
 {
  "type": "ReplayRequestAction",
  "target": "https://bob.example.com/inbox/", // container path must end with slash
  "startDate": "2022-09-29T09:00:00.000Z", // ISO date (must be before endDate)
  "endDate": "2022-09-29T10:00:00.000Z" // ISO date (must be after startDate)
 }
```

## output to replay-start queue
```json
 {
  "type": "ReplayStartAction",
  "startPrefix": "s3://[SOLID_BUCKET_NAME]/[bob.example.com]/data/by-ts/inbox/2022/09/29/09/",
  "endPrefix": "s3://[SOLID_BUCKET_NAME]/[bob.example.com]/data/by-ts/inbox/2022/09/29/10/"
 }
```

## return response
```json
 {
  "type": "ReplayStartAction",
  "actionStatus": "ActiveActionStatus" || "FailedActionStatus",
  "description": "Replay Queued",
  "error": {
    "message": "startDate is after endDate"
  }
 }
```