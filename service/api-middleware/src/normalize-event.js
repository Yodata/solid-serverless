// @ts-check

const logger = require('./lib/logger')
const getHeaders = require('./lib/get-headers')
const getHeader = require('./lib/get-header-value')
const getData = require('./lib/get-event-data')
const hasData = require('./lib/request-has-data')

/**
 * @typedef NormalizeEventResponse
 * @property {string} stage
 * @property {boolean} hasData
 * @property {string}	contentType
 * @property {object}	object
 */

/**
 * normalize event.request and response
 * @param {object} 	event
 * @param {object} 	event.request - http request object
 * @param {object}	[event.response] http response object
 * @param {string} 	[event.stage] - request | response
 * @param {boolean} [event.hasData]
 * @param {string} 	[event.contentType]
 * @param {object} 	[event.object]
 * @returns {object}
 */
module.exports = (event) => {
	// set event.stage = request|response
	event.stage = event.response ? 'response' : 'request'
	const req = event[event.stage]
	// normalize headers
	if (req && !req.headers && req.rawHeaders) {
		req.headers = getHeaders(req)
	}
	event.hasData = hasData(req)
	event.contentType = getHeader(req, 'content-type')
	event.object = getData(event)
	logger.debug('normalize-event:result', { event })
	return event
}
