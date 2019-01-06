// @ts-check

const logger = require('./lib/logger')
const processRequest = require('./process-request')

/**
 * @typedef ApiMiddlewareResponse
 * @property {object}  request  - httpRequest object
 * @property {object}  response	- httpResponse
 * @property {string}  stage 	- request | resposne
 * @property {boolean} hasData	- 
 * @property {object}  object 	- JSON.parse of the response or request.body
 */

/**
 * @param {object} event
 * @param {object} event.request 
 * @param {object} [event.response]
 * @returns {Promise<ApiMiddlewareResponse>}
 */
exports.handler = async event => {
	try {
		logger.debug('api-middleware:event-received', {event})
		event = await processRequest(event)
	} catch (error) {
		logger.error(error.message, {stack: error.stack})
	}
	logger.info('api-middleware:result', {event})
	// @ts-ignore
	return event
}
