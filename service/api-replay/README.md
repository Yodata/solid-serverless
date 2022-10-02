# replay service
provides and API for initiating a replay on a specific container between two date-time points

## data flow
1. [api](src/http/post-replay/README.md) - takes target, startDate endDate and sends to replay-start queue
2. [replay-start](src/queues/replay-start/README.md) - gets replay items (batch) and sends to replay-items queue
3. [replay-items](src/queues/replay-items/README.md)



## environment variables

ARC_APP_SECRET: (required) any strong random string for encryption key
LOG_LEVEL: (optional) debug | info | warn | error | silent
REPLAY_BATCH_SIZE (optional) 1-1000 (default: 100)
REPLAY_ITEM_CONCURRENCY 1-10 (default: 5)
REPLAY_ITEM_LIMIT (optional) default = 10000 | -1 for unlimited items
SOLID_HOST (required) hostname of the root pod for the environment
SOLID_STORE (required) bucket name of the store for the environment
SVC_KEY (required) solid admin key for the environment

use @architect/arc to manage environment variables

```shell
$ arc env [-e staging|production|testing] [--add|--remove] VARIABLE_NAME VARIABLE_VALUE
```
### SOLID-DEV
| VAR NAME                | VALUE                               |
|-------------------------|-------------------------------------|
| ARC_APP_SECRET          | secret                              |
| LOG_LEVEL               | debug                               |
| REPLAY_BATCH_SIZE       | 10                                  |
| REPLAY_ITEM_CONCURRENCY | 5                                   |
| REPLAY_ITEM_LIMIT       | 100                                 |
| SOLID_HOST              | root.example.com                    |
| SOLID_STORE             | name of the solid storage bucket    |
| SVC_KEY                 | secret                              |

### PRODUCTION
| VAR NAME                | VALUE                                |
|-------------------------|--------------------------------------|
| ARC_APP_SECRET          | secret                               |
| LOG_LEVEL               | info                                 |
| REPLAY_BATCH_SIZE       | 100                                  |
| REPLAY_ITEM_CONCURRENCY | 5                                    |
| REPLAY_ITEM_LIMIT       | 10000                                |
| SOLID_HOST              | root.example.com                     |
| SOLID_STORE             | {cust}-{env}-solid-serverless-store  |
| SVC_KEY                 | secret                               |
|-------------------------|--------------------------------------|