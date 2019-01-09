// @ts-check

const logger = require('./lib/logger')
const processRequest = require('./process-request')
const finalize = require('./finalize-event')

/**
 * @typedef ApiMiddlewareResponse
 * @property {object}  request  - httpRequest object
 * @property {object}  response	- httpResponse
 * @property {string}  stage 	- request | resposne
 * @property {boolean} hasData
 * @property {object}  object 	- JSON.parse of the response or request.body
 */

/**
 * @param {object} event
 * @param {object} event.request 
 * @param {object} [event.response]
 * @param {object} event.object
 * @returns {Promise<ApiMiddlewareResponse>}
 */
exports.handler = async (event,context) => {
	logger.debug('api-middleware:event-received', {event,context})
	try {
		event = await processRequest(event)
	} catch (error) {
		logger.error('api-middleware:error', {event,context,error})
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
	} finally {
		event = finalize(event)
		logger.info('api-middleware:result', {event})
	}
	// @ts-ignore
	return event
}
