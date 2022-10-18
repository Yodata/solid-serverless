# replay-items-service

Takes a target and list of items to replay and an optional filter and replays them with yodata.touch

## input

```json5
{
  "target": "https://ace.bhhs.hsfaffiliates.com/inbox/",
  "items": [
      "fe8c1ff2f196441e8d226ffae1f225a9",
      "5e76a09475324bb6af67f90e629fd009"
    ],
    "filter:" {
      "$eq": {
        "topic": "realestate/listing#update"
      }
    }
}
```


## result

A maximum of two objects woul be replayed if they both match the filter criteria.


## configuration

### required environment variables

| NAME                     | EXAMPLE             | DESCRIPTION                                                |
|--------------------------|---------------------|------------------------------------------------------------|
| SOLID_HOST               | https://example.com | pod host for the replay client                             |
| SVC_KEY                  | xxxx                | service credentials                                        |
| REPLAY_BATCH_SIZE        | 100                 | max itmes allowed per requeset (default 100)               |
| REPLAY_FILTERING_ENABLED | false               | set to true to enable filtering                            |
| REPLAY_ITEM_CONCURRENCY  | 3                   | number of clients to open for replay requests (default: 3) |

## filtering
if REPLAY_FILTERING_ENABLED = true, the service will only replay messages that match the filter.
see [queryl](https://github.com/jviotti/queryl) for filter syntax.