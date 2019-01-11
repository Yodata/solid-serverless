// @ts-check

const winston = require('winston')

const format = winston.format.combine(
	winston.format.simple()
)

const transports = [
	new winston.transports.Console()
]

const defaultLogLevel = () => {
	let level
	switch (process.env.NODE_ENV) {
	case 'development':
		level = 'debug'
		break
	case 'production':
		level = 'info'
		break
	default:
		level = 'info'
	}
	return level
}

const level = process.env.DEBUG_LEVEL || defaultLogLevel()

const defaultOptions = {format, transports, level}

exports.defaultLogger = winston.createLogger(defaultOptions)
exports.createLogger = options => winston.createLogger(Object.assign({},defaultOptions,options))
exports.format = winston.format
exports.transports = winston.transports
