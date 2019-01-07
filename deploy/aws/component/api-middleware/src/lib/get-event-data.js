const getHeader = require('./get-header-value')
const typeis = require('type-is')

const parseBody = (req) => {
	const buff = new Buffer(req.body,'base64').toString()
	return JSON.parse(buff)
}

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
		return parseBody(req)
	case 'application/ld+json':
		return parseBody(req)
	default:
		return null
	}
}