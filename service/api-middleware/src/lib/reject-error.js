const logger = require('./logger')

/**
 *
 * @param {*} message
 * @param {*} event
 */
module.exports = function (message, event) {
	message = String(message)
	logger.error(message, event)
	return Promise.reject(new Error(message))
}