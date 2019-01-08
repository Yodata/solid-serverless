const winston = require('winston')

const format = winston.format.combine(
	winston.format.simple()
)

const transports = [
	new winston.transports.Console()
]

const level = process.env.DEBUG_LEVEL || process.env.NODE_ENV === 'production' ? 'info' : 'debug'

const defaultOptions = {format, transports, level}

exports.defaultLogger = winston.createLogger(defaultOptions)
exports.createLogger = options => winston.createLogger(Object.assign({},defaultOptions,options))
