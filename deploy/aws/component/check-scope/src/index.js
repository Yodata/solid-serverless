// @ts-check

const logger = require('./lib/logger')
const checkScope = require('./check-scope')

/**
 * validates event.object with event.scope returning event.isAllowed {boolean}
 * @param {object} event
 * @param {object} event.object   - data to be tested
 * @param {object} event.scope    - the ACL.scope value
 * @param {boolean} [event.isAllowed]
 * @returns {Promise<CheckScopeResponse>}
 * 
 * @typedef CheckScopeResponse
 * @property {object} object
 * @property {object} scope
 * @property {object} result
 * @property {boolean} result.isAllowed
 * @property {object} [error]
 */
exports.handler = async (event, context) => {
	logger.debug('check-scope:received', event)
	logger.debug('check-scope:context', context)
	let response
	try {
		response = await checkScope(event)
	} catch (error) {
		logger.error('check-scope:error', {error})
		response = {error}
	} finally {
		logger.debug('check-scope:response', response)
	}
	return response
}
