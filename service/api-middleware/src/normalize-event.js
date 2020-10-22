// @ts-check

const logger = require('./lib/logger')
const getHeaders = require('./lib/get-headers')
const getHeader = require('./lib/get-header-value')
const getData = require('./lib/get-event-data')
const hasData = require('./lib/request-has-data')

/**
 * @typedef NormalizeEventResponse
 * @property {object}  request - http.request
 * @property {object}	 [response] - http.response
 * @property {string}  stage - request | response
 * @property {boolean} hasData - true if event has parsed data.object
 * @property {string}	 contentType - mapi.contenttype
 * @property {object}	 [object] - parsed event body
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
 *
 * @returns {Promise<NormalizeEventResponse>}
 */
module.exports = async (event) => {
	// set event.stage = request|response
	event.stage = event.response ? 'response' : 'request'
	const message = event[event.stage]
	// normalize headers
	if (message && !message.headers && message.rawHeaders) {
		message.headers = getHeaders(message)
	}
	event.hasData = hasData(message)
	if (event.hasData) {
		event.contentType = getHeader(message, 'content-type')
		event.object = getData(event)
	}
	logger.debug('normalize-event:result', { event })
	return event
}
