const winston = require('winston')
const select = require('./select-properties')

const formatRequest = winston.format(
	select('event.request', 'method,headers,url,body,isBase64Encoded')
)

const format = winston.format.combine(
	formatRequest(),
	winston.format.simple()
)

const transports = [
	new winston.transports.Console()
]

const level = process.env.DEBUG_LEVEL || process.env.NODE_ENV === 'production' ? 'info' : 'debug'

const defaultOptions = {format, transports, level}

const defaultLogger = winston.createLogger(defaultOptions)

module.exports = defaultLogger

