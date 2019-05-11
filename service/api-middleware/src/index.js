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
 * @param {object} event.request 
 * @param {object} [event.response]
 * @param {object} event.object
 * @returns {Promise<ApiMiddlewareResponse>}
 */
exports.handler = async (event, context) => {
	logger.debug('event-received', { event, context })
	try {
		event = await processRequest(event)
	} catch (error) {
		logger.error(`ERROR:${error.message}`, { error })
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
	}
	event = finalize(event)
	logger.info('api-middleware:result', event)
	return event
}
