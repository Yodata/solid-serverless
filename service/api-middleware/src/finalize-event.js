// @ts-check

const logger = require('./lib/logger')
const getHeader = require('./lib/get-header-value')
// const mimetype = require('type-is')

const encode = (data) => Buffer.from(JSON.stringify(data)).toString('base64')

module.exports = (event) => {
	if (event.hasData && event.object) {
		let stage = event.response ? 'response' : 'request'
		const req = event[ stage ]
		const contentType = getHeader(req, 'content-type', event.contentType)
		if (String(contentType).includes('json')) {
			req.body = encode(event.object)
			req.isBase64Encoded = true
		} else {
			const message = 'MISSING_HEADER_CONTENT_TYPE'
			logger.error(message, event)
		}
	}
	return event
}