const logger = require('./logger')

/**
 * Create an async handler with error handling and logging
 * @param {function} fn - an async handler function
 * @param {string} name - handler name
 */
const createHandler = (fn, name) => async (event, context) => {
	try {
		logger.debug(`start:${name}`, {event, context})
		event = await fn(event)
	} catch (error) {
		logger.error(`error:${name}`, {error, event, context})
	}
	logger.info(`completed:${name}`, {name, event, context})
	return event
}

module.exports = createHandler
