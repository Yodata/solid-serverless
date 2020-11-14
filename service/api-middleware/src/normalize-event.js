// @ts-check

const logger = require('./lib/logger')
const getHeaders = require('./lib/get-headers')
const getHeader = require('./lib/get-header-value')
const getData = require('./lib/get-event-data')
const requestHasData = require('./lib/request-has-data')
const has = require('./lib/object-has')
const reject = require('./lib/reject-error')

/**
 * @typedef NormalizeEventResponse
 * @property {object}  request - http.request
 * @property {object}	 [response] - http.response
 * @property {string}  stage - request | response
 * @property {boolean} hasData - true if event has parsed data.object
 * @property {string}	 contentType - mapi.contenttype
 * @property {object}  event.headers
 * @property {object}	 [object] - parsed event body
 */

/**
 * normalize event.request and response
 * @param {object} 	event
 * @param {object}  event.headers
 * @param {object} 	event.request - http request object
 * @param {object}	[event.response] http response object
 * @param {string} 	event.stage - request | response
 * @param {string} 	event.agent - the uri of the request agent
 * @param {boolean} event.hasData
 * @param {string} 	event.contentType
 * @param {object} 	[event.object]
 *
 * @returns {Promise<NormalizeEventResponse>}
 */
module.exports = async (event) => {
	if (!event) {
		return reject('normalize-event:error:event-undefined', event)
	}
	if (!has(event, 'request')) {
		return reject('normalize-event:error:missing-request', event)
	}
	if (!has(event, 'agent')) {
		if (has(event, 'request.solidService', true)) {
			event.agent = `https://solid-service-agent.${process.env.SOLID_HOST}/profile/card#me`
		}
	}
	// set event.stage = request|response
	event.stage = event.response ? 'response' : 'request'

	const message = event[ event.stage ]
	// normalize headers
	if (message && !message.headers && message.rawHeaders) {
		message.headers = getHeaders(message)
	}
	event.hasData = requestHasData(message)
	message.hasData = requestHasData(message)
	if (event.hasData) {
		event.contentType = getHeader(message, 'content-type')
		message.contentType = event.contentType
		event.object = getData(event)
		message.object = event.object
	}
	return event
}
