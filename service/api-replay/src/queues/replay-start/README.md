# replay-start-queue-lamda

1. receives replay start actions
2. gets s3 objects starting from startPrefix in batches of 1000
3. sends batch to replay-objects-queue to be replayed.
4. stops when last object in batch is after endPrefix

note: props are JSON buy I like yaml so examples are shown in yaml. deal with it. please.

## input parameters
```yaml
  type: ReplayRequestAction
  target: 'https://example.com/inbox/'
  startDate: '2022-09-30T02:09:16.483Z'
  endDate: '2022-09-30T03:09:16.483Z'
```

## transformed input parameters
```yaml
  type: ReplayRequestAction
  bucket: process.env.SOLID_STORE
  prefix: entities/${target.host}/data/by-ts/${target.path}/year/month/day/
  startPath: entities/${target.host}/data/by-ts/${target.path}/year/month/day/hours/minutes/
  endPath: entities/${target.host}/data/by-ts/${target.path}/year/month/day/hours/minutes/
```

##