const createLogger = require('@yodata/solid-serverless-logger').createLogger
const winston = require('winston');
const transports = [
    new winston.transports.Console()
]
const getFormat = () => {
    if (process.env.NODE_ENV === 'production') {
        return winston.format.combine(
            winston.format.timestamp(),
            winston.format.logstash()
        )
    } else {
        return winston.format.prettyPrint()
    }
}
const format = getFormat()
const level = process.env.NODE_ENV === 'production' ? 'info': 'debug'

const winstonConfig = { format, transports, level }
module.exports = createLogger(winstonConfig)