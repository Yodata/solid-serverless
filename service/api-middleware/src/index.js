// @ts-check

const logger = require('./lib/logger')
const processRequest = require('./process-request')
const finalize = require('./finalize-event')

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
exports.handler = async (event) => {
	event = await processRequest(event)
		.then(result => {
			logger.debug('api-middleware:result', result)
			return result
		})
		.catch(error => {
			logger.error(`api-middleware:error:${error.message}`)
			event.response = {
				status: 500,
				statusCode: '500',
				end: true
			}
			event.object = {
				'error': {
					'message': error.message
				}
			}
			return event
		})
	return finalize(event)
}
