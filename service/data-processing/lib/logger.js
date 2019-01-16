const winston = require('winston')

const format = winston.format.combine(
	winston.format.timestamp(),
	winston.format.logstash()
)
const transports = [
	new winston.transports.Console()
]
const logger = winston.createLogger({format, transports})

module.exports = logger
