const logger = require('./logger')

/**
 * Create an async handler with error handling and logging
 * @param {function} fn - an async handler function
 * @param {string} name - handler name
 */
const createHandler = (fn, name) => async (event, context) => {
	const { object } = event
	try {
		logger.debug(`start:${name}`, { object, context })
		event = await fn(event)
	} catch (error) {
		logger.error(`error:${name}:${error.message}`, { object })
	}
	logger.info(`completed:${name}`, { object })
	return event
}

module.exports = createHandler
