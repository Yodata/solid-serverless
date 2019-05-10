const logger = require('./logger')
const getHeader = require('./get-header-value')
const typeis = require('type-is')
const parseJson = require('parse-json')
const yaml = require('js-yaml')


const parseBody = (req) => {
	let body = req.body
	let data
	if (body.length === 0) {
		return {}
	}
	if (req.isBase64Encoded) {
		body = new Buffer(req.body, 'base64').toString()
	}
	return body
}

/**
 * @param {object} event
 * @param {object} event.object
 * @param {string} event.stage
 * @param {boolean} event.hasData
 * @param {object} [event.request]
 * @param {object} [event.response]
 */
module.exports = (event) => {
	logger.debug('get-event-data:received', { event })
	let stage = event.response ? 'response' : 'request'
	if (stage === event.stage && event.hasData && event.object) {
		logger.debug('get-event-data:using-event.object', { object: event.object })
		return event.object
	}
	const req = event[stage]
	const contentType = getHeader(req, 'content-type')
	let data
	switch (typeis.is(contentType, ['json', '+json', 'application/x-yaml'])) {
		case 'json':
			data = parseJson(parseBody(req))
			break
		case 'application/ld+json':
			data = parseJson(parseBody(req))
			break
		case 'application/x-yaml':
			data = yaml.load(parseBody(req))
			break
		default:
			data = null
	}
	logger.debug('get-event-data:result', { data })
	return data
}