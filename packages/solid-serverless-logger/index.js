
const logger = require('@yodata/logger')

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
const level = process.env.LOG_LEVEL || defaultLogLevel()

logger.defaultLogger = logger
logger.createLogger = () => logger.createLogger(console.log, level)
logger.format = {
	default: props => props
}
logger.transports = {
	default: props => props
}


module.exports = logger