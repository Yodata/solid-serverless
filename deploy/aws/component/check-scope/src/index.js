// @ts-check

const logger = require('./lib/logger')
const checkScope = require('./check-scope')

/**
 * @typedef CheckScopeResponse
 * @property {object} object - the tested value
 * @property {object} scope - the scope used to test the object
 * @property {boolean} isAllowed - true if allowed
 */

/**
 * validates event.object with event.scope returning event.isAllowed {boolean}
 * @param {object} event
 * @param {object} event.object   - data to be tested
 * @param {object} event.scope    - the ACL.scope value
 * @param {boolean} [event.isAllowed]
 * @returns {Promise<object>}
 * 
 * @example response
 * {
 *   object: {}
 *   scope: {}
 *   isValid: true
 * }
 */
exports.handler = async (event, context) => {
	try {
		logger.debug('check-scope:received', {event, context})
		let result = await checkScope(event)
		event.isAllowed = result.isAllowed
	} catch (error) {
		logger.error('check-scope:error', {error})
		event.object = error
		event.isAllowed = false
	}
	logger.info('check-scope:response', {event})
	return event	
}
