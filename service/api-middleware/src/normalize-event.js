// @ts-check

const logger = require('./lib/logger')
const getHeaders = require('./lib/get-headers')
const getHeader = require('./lib/get-header-value')
const getData = require('./lib/get-event-data')
const requestHasData = require('./lib/request-has-data')
const has = require('./lib/object-has')
const reject = require('./lib/reject-error')
const get = require('get-value')

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
 * @param {object} 	[event.object] - if event.hasData = true then the parsed body is here.
 *
 * @returns {Promise<NormalizeEventResponse>}
 */
module.exports = async (event) => {
	// no event throws
	if (!event) {
		return reject('normalize-event:error:event-undefined', event)
	}
	// no request throws
	if (!has(event, 'request')) {
		return reject('normalize-event:error:missing-request', event)
	}
	// no api keys should be in the logs
	if (has(event, 'request.rawHeaders.x-api-key')) {
		delete event.request.rawHeaders['x-api-key']
	}

	if (!has(event, 'agent')) {
		if (has(event, 'request.solidService', true)) {
			event.agent = `https://solid-service-agent.${process.env.SOLID_HOST}/profile/card#me`
		}
	}

	if (
		has(event, 'request.target.path', v => String(v).startsWith('/outbox/')) &&
		has(event, 'request.method', v  => ['PUT','POST'].includes(v)) &&
		has(event, '@to')
	) { event.agent = event[ '@to' ]}

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

	logger.debug('api-middleware:normalize-event:result', {
		agent: event.agent,
		method: get(event, 'request.method'),
		target: get(event, 'request.url'),
		stage: event.stage,
		contentType: event.contentType,
		object: event.object
	})

	return event
}
