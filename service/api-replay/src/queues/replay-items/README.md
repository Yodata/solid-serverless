# replay-items-service

Takes a target and list of items to replay and an optional filter and replays them with yodata.touch

## input

```json5
{
  "target": "https://ace.bhhs.hsfaffiliates.com/inbox/",
  "object": {
    "items": [
      "fe8c1ff2f196441e8d226ffae1f225a9",
      "5e76a09475324bb6af67f90e629fd009"
    ],
    "filter:" {
      "topic": ""
      "data.object.id": "https://12345*"
    }
  }
}
```


## result

A maximum of two objects woul be replayed if they both match the filter criteria.
