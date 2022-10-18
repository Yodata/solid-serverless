# replay-start-queue-lamda

1. receives replay start actions
2. gets s3 objects starting from startPrefix in batches of 1000
3. sends batch to replay-objects-queue to be replayed.
4. stops when last object in batch is after endPrefix

note: props are JSON buy I like yaml so examples are shown in yaml. deal with it. please.

## input parameters

### time bounded request (contains startDate and endDate)

```yaml
  type: ReplayRequestAction
  target: 'https://example.com/inbox/'
  startDate: '2022-09-30T02:09:16.483Z'
  endDate: '2022-09-30T03:09:16.483Z'
```

### specific item request (maxiumu of about 5000 items)

```yaml
  type: ReplayRequestAction
  target: 'https://example.com/inbox/'
  items:
    - 5e76a09475324bb6af67f90e629fd011
    - 5e76a09475324bb6af67f90e629fd012
    - 5e76a09475324bb6af67f90e629fd013
  filter: ## optional filter
    $contains:
      data.object.type: RealEstateAgent
```

## transformed input parameters

```yaml
  type: ReplayRequestAction
  bucket: process.env.SOLID_STORE
  prefix: entities/${target.host}/data/by-ts/${target.path}/year/month/day/
  startPath: entities/${target.host}/data/by-ts/${target.path}/year/month/day/hours/minutes/
  endPath: entities/${target.host}/data/by-ts/${target.path}/year/month/day/hours/minutes/
```

## sends to replay items queue (in batches of process.env.REPLAY_BATCH_SIZE)

```yaml
type: ReplayRequestAction
target: 'https://bob.example.com/inbox/'
items:
  - 5e76a09475324bb6af67f90e629fd009
  - 5e76a09475324bb6af67f90e629fd010
  - 5e76a09475324bb6af67f90e629fd011
 filter: ## optional filter
  $contains:
    data.object.type: RealEstateAgent
```
