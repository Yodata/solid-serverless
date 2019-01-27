// @ts-check
const logger = console
const createView = require('./lib/create-view')

/**
 * transforms event.object with event.context (a @yodata/transform.Context)
 * @param {object} event
 * @param {object} event.object - the data to be transformed
 * @param {object} event.context - the context definition object
 * @returns {Promise<object>} - event.object (transformed)
 */
exports.handler = async (event, context) => {
	logger.debug('event-received', event)
	let result = event.object
	try {
		// result = createView(event)
		result = event.object
	} catch (error) {
		logger.error('error', {error, context})
	}
	logger.debug('result', result)
	return result
}
