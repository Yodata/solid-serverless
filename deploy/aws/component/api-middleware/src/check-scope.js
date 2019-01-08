// @ts-check

const logger = require('./lib/logger')
const invoke = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')

/**
 * checks event using event.scope, adds event.isAllowed {boolean}
 * @param {object} 	event
 * @param {object} 	event.object
 * @param {object} 	event.scope
 * @param {boolean}	event.isAllowed
 * @param {object}	[event.request]
 * @param {object}	[event.response]
 * @returns {Promise<object>}
 */
module.exports = async (event) => {
	if (hasScope(event)) {
		logger.debug('api-middleware:check-scope:received', {event})
		const functionName = getEnvValue(event,'CHECK_SCOPE_FUNCTION_NAME', 'check-scope')
		const params = getScopeParams(event)
		event.isAllowed = await invoke(functionName,params).then(res => {
			logger.debug('check-scope-response', res)
			return res.isAllowed
		})
		if (event.isAllowed === false) {
			event.object = {'error':'rejected by scope'}
			event.response = {
				status: '403',
				statusCode: 403,
				end: true
			}
		}
	}
	logger.debug('api-middleware:check-scope:result', event['object'])
	return event
}

const getScopeParams = (event) => {
	return {
		object: event,
		scope: event.scope
	}
}


const hasScope = (event) => {
	return event && event.scope && typeof event.scope === 'object' && Object.keys(event.scope).length > 0
}