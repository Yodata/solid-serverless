// @ts-check

const logger = require('./lib/logger')
const getHeader = require('./lib/get-header-value')
const typeis = require('type-is')

module.exports = (event) => {
	if (event.hasData && event.object) {
		let stage = event.response ? 'response' : 'request'
		const req = event[stage]
		const contentType = getHeader(req,'content-type')
		switch(typeis.is(contentType,['json','+json'])) {
		case 'json':
			req.body = JSON.stringify(event.object)
			break
		case 'application/ld+json':
			req.body = JSON.stringify(event.object)
			break
		}
		logger.info('finalize request', {req})
	}
	
	return event
}