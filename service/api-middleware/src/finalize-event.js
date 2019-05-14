// @ts-check

const logger = require('./lib/logger')
const getHeader = require('./lib/get-header-value')
const mimetype = require('type-is')

const encode = (data) => new Buffer(JSON.stringify(data)).toString('base64')

module.exports = (event) => {
	if (event.hasData && event.object) {
		let stage = event.response ? 'response' : 'request'
		const req = event[stage]
		const contentType = getHeader(req, 'content-type')
		switch (mimetype.is(contentType, ['json', '+json'])) {
			case 'json':
				req.body = encode(event.object)
				req.isBase64Encoded = true
				break
			case 'application/ld+json':
				req.body = encode(event.object)
				req.isBase64Encoded = true
				break
		}
	}
	logger.debug('finalize-event.object', { event })
	return event
}