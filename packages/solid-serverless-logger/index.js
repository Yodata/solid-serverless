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

const defaultOptions = {format,transports,level}

exports.defaultLogger = winston.createLogger(defaultOptions)
exports.createLogger = options => winston.createLogger({...defaultOptions, ...options})