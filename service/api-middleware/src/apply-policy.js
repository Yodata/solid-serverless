// @ts-check

const logger = require('./lib/logger')
const invoke = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')
const has = require('./lib/object-has')
const requestHasData = require('./lib/request-has-data')

const isJSON = (event) => {
	return String(event.contentType).toLowerCase().includes('json')
}

// event must have a policy to apply (duh)
const hasPolicy = (event) => {
	return has(event, 'policy', v => (typeof v === 'object' && Object.keys(v).length > 0))
}
// event must have data
const hasData = (event) => {
	const stage = has(event, 'response') ? 'response' : 'request'
	return requestHasData(event[stage])
}

// we don't want to run data policies on data policy documents or requests made by the data policy service
const dataPolicyRequest = (event) => {
	const DATA_POLICY_PATH = getEnvValue(event, 'DATA_POLICY_PATH', '/public/yodata/data-policy.json')
	const SOLID_HOST = getEnvValue(event, 'SOLID_HOST', 'bhhs.dev.yodata.io')
	const DATA_POLICY_SVC_HOST = getEnvValue(event, 'DATA_POLICY_SVC_HOST', `https://dps.${SOLID_HOST}/profile/card#me`)
	return (
		has(event, 'request.target.path', DATA_POLICY_PATH)
		|| has(event, 'agent', DATA_POLICY_SVC_HOST)
	)
}

exports.isDataPolicyRequest = dataPolicyRequest
exports.hasData = hasData
exports.hasPolicy = hasPolicy
exports.isJSON = isJSON


/**
 * Apply data policies
 * @param {object} event
 * @param {object} event.request - the HTTP.Request
 * @param {object} [event.response] - HTTP.Response
 * @param {object} event.object - the data in JSON format to be applied
 * @param {object} [event.policy]
 */
exports.applyDataPolicy = async (event) => {
	if (!dataPolicyRequest(event) && isJSON(event) && hasPolicy(event) && hasData(event)) {
		const functionName = getEnvValue(event, 'APPLY_POLICY_FUNCTION_NAME', 'apply-policy')
		event.object = await invoke(functionName, event).then(response => response.object)
		logger.debug('apply-policy:result', { event })
	} else {
		logger.debug('apply-policy:skipped', { event })
	}
	return event
}