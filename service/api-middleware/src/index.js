// @ts-check

const logger = require('./lib/logger')
const processRequest = require('./process-request')
const finalize = require('./finalize-event')
const get = require('get-value')

/**
 * @typedef ApiMiddlewareResponse
 * @property {object}  request  - HttpRequest object
 * @property {object}  [response]	- HttpResponse object
 * @property {string}  [stage] - request/response
 * @property {boolean} [hasData] - true if the event includes JSON data
 * @property {object}  [object] - JSON.parse of the response or request.body
 */

/**
 * @param {object} event
 * @param {object} event.object  - the primary subject of this event
 * @param {object} event.request - the http request
 * @param {object} [event.response] - the http response
 * @param {object} [event.scope] - the event scope (permissions)
 * @param {object} [event.policy] - data policies for the request
 * @returns {Promise<ApiMiddlewareResponse>}
 */

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
exports.handler = async (event) => {
	event = await processRequest(event)
		.then(next => {
			logger.info('api-middleware:result', {
				agent: get(next, 'agent'),
				method: get(next, 'request.method'),
				target: get(next, 'request.url'),
				stage: get(next, 'stage'),
			})
			return next
		})
		.catch(error => {
			const { message, stack } = error
			event.response = {
				status: 500,
				statusCode: '500',
				end: true
			}
			event.object = {
				'error': {
					'message': message
				}
			}
			logger.error(`api-middleware-error:${message}`, {
				agent: get(event, 'agent'),
				method: get(event, 'request.method'),
				target: get(event, 'request.url'),
				stage: get(event, 'stage'),
				object: get(event, 'object'),
				error: { message, stack }
			})
			return event
		})
	return finalize(event)
}
