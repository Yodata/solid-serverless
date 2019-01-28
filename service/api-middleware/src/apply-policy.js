// @ts-check

const logger = require('./lib/logger')
const invoke = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')

/**
 * Apply data policies
 * @param {object} event
 * @param {object} event.request - the HTTP.Request
 * @param {object} [event.response] - HTTP.Response
 * @param {object} event.object - the data in JSON format to be applied
 * @param {object} [event.policy]
 */
module.exports = async (event) => {
	if (hasPolicy(event) && hasData(event)) {
		const functionName = getEnvValue(event,'APPLY_POLICY_FUNCTION_NAME', 'apply-policy')

		event.object = await invoke(functionName,event).then(response => response.object)
		logger.debug('apply-policy:result', {event})
	} else {
		logger.debug('apply-policy:skipped', {event})
	}
	return event
}


const hasPolicy = (event) => {
	return event && event.policy && typeof event.policy === 'object' && Object.keys(event.policy).length > 0
}

const hasData = (event) => {
	return (typeof event.hasData !== 'undefined') ? event.hasData : (typeof event.object !== 'undefined')
}
