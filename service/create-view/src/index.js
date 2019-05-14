// @ts-check
const logger = require('./lib/logger')
const createView = require('./lib/create-view')

/**
 * @typedef Action
 * @property {string} type
 * @property {string|object} agent
 * @property {string|object} object
 * @property {string|object} instrument
 * 
 * @typedef Event
 * @property {string} topic
 * @property {Action} data
 * 
 * @typedef Message
 * @property {Event} object
 * @property {object} [scope]
 * @property {object} [context]
 * 
 * transforms event.object with event.context (a @yodata/transform.Context)
 * @param {Message} event
 * @returns {Promise<object>} - event.object (transformed)
 */
exports.handler = async (event, context) => {
	let result
	try {
		logger.info('create-view:received', event['object'])
		result = await createView(event)
	} catch (error) {
		logger.error('create-view:error', { error, event, context })
		logger.error('event data returned unchanged')
		result = event.object
	}
	logger.info('create-view:result', result)
	return result
}
