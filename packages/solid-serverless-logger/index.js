const winston = require('winston');
const format = winston.format.combine(
    winston.format.timestamp(),
    winston.format.logstash()
)
const transports = [
    new winston.transports.Console()
]

const defaultLogger = winston.createLogger({ format, transports, level: 'debug' });

exports.defaultLogger = defaultLogger
exports.createLogger = config => winston.createLogger(config)