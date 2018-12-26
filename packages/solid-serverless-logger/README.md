# `@yodata/solid-serverless-logger`

Default logger for aws Cloudwatch format is logstach in production, console/JSON in development.

## Usage

```javascript
const {createLogger} = require('@yodata/solid-serverless-logger')
const namespace = 'myservice'
const meta = {
    version: 'xxx'
}
const logger = createLogger(namespace, meta)

const message = 'log message'
const data = {a: 1}
logger.debug(message,data)
//-> writes to the log
// 'message', {a: 1, version: 'xxx'}
```
