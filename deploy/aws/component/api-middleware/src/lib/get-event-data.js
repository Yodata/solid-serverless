const getHeader = require('./get-header-value')
const typeis = require('type-is')

/**
 * @param {object} event
 * @param {object} [event.request]
 * @param {object} [event.response]
 */
module.exports = (event) => {
	let stage = event.response ? 'response' : 'request'
	if (stage === event.stage && event.hasData && event.object) {
		return event.object
	}
	const req = event[stage]
	const contentType = getHeader(req,'content-type')
	switch(typeis.is(contentType,['json','+json'])) {
	case 'json':
		return JSON.parse(req.body)
	case 'application/ld+json':
		return JSON.parse(req.body)
	default:
		return null
	}
}