// @ts-check

const logger = require('./lib/logger')
const invoke = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')
const requestHasData = require('./lib/request-has-data')

/**
 * Apply data policies
 * @param {object} event
 * @param {object} event.request
 * @param {object} [event.response]
 * @param {object} event.object
 * @param {object} [event.policy]
 */
module.exports = async (event) => {
	if (hasPolicy(event) && hasData(event)) {
		logger.debug('apply-policy:start', {event})
		const functionName = getEnvValue(event,'APPLY_POLICY_FUNCTION_NAME', 'apply-policy')
		const params = {object: event.object, policy: event.policy}
		event.object = await invoke(functionName,params).then(response => response.object)
	}
	logger.debug('apply-policy:result', {event})
	return event
}


const hasPolicy = (event) => {
	return event && event.policy && typeof event.policy === 'object' && Object.keys(event.policy).length > 0
}

const hasData = (event) => {
	return (typeof event.hasData !== 'undefined') ? event.hasData : requestHasData(event[event.stage])
}
