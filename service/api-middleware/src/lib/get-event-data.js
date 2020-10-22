const logger = require('./logger')
const getHeader = require('./get-header-value')
const typeis = require('type-is')
const parseJson = require('parse-json')
const yaml = require('js-yaml')


const parseBody = (req) => {
	let body = String(req.body)
	if (body.length === 0) {
		return '{}'
	}
	if (req.isBase64Encoded) {
		body = Buffer.from(req.body, 'base64').toString()
	}
	return body
}

/**
 * @param {object}  event
 * @param {boolean} event.hasData
 * @param {string}  event.stage
 * @param {object}  [event.object]
 * @param {object}  [event.request]
 * @param {object}  [event.response]
 *
 * @returns {*} event.object
 */
module.exports = (event) => {
	logger.debug('get-event-data:received', { event })
	let data

	const stage = event.response ? 'response' : 'request'
	if (stage === event.stage && event.hasData && event.object) {
		// event already has data (event.object)
		data = event.object
	} else {
		const req = event[stage]
		const contentType = getHeader(req, 'content-type')
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
	}
	logger.debug('get-event-data:result', { data })
	return data
}