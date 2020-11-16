// @ts-check

const logger = require('./lib/logger')
const invoke = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')
const has = require('./lib/object-has')
const { URL } = require('url')
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
	return requestHasData(event[ stage ])
}

// we don't want to run data policies on data policy documents or requests made by the data policy service
// temporary fix for performance and data policy issues is only call the service on data profile reads
const dataPolicyRequest = (event) => {
	const DATA_POLICY_PATH = getEnvValue(event, 'DATA_POLICY_PATH', '/public/yodata/data-policy.json')
	const SOLID_HOST = getEnvValue(event, 'SOLID_HOST', 'bhhs.dev.yodata.io')
	const DATA_POLICY_SVC_HOST = getEnvValue(event, 'DATA_POLICY_SVC_HOST', `dps.${SOLID_HOST}`)
	return (
		has(event, 'request.target.path', DATA_POLICY_PATH)
		|| has(event, 'agent', v => (String(v).includes(DATA_POLICY_SVC_HOST)))
	)
}

const isWhiteListed = (event) => {
	const { DATA_POLICY_SVC_HOST, DATA_POLICY_WL, SOLID_HOST = 'bhhs.dev.yodata.io' } = process.env
	const { agent } = event

	if (typeof agent !== 'string') return false

	const host = String(agent).startsWith('http') ? new URL(agent).hostname : agent

	if (host === SOLID_HOST) {
		return true
	}
	if (host === DATA_POLICY_SVC_HOST) {
		return true
	}
	if (String(DATA_POLICY_WL).length > 0) {
		const whitelist = String(DATA_POLICY_WL).split(',')
		const index = whitelist.findIndex((v) => (String(host).includes(v)))
		return (index >= 0) ? true : false
	}
	return false
}

const isProfileReadEvent = (event) => {
	return (
		has(event, 'request.target.path', '/profile/card') &&
		has(event, 'request.method', 'GET')
	)
}

exports.isProfileReadEvent = isProfileReadEvent
exports.isWhiteListed = isWhiteListed
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
 * @param {object} [event.error]
 */
exports.applyDataPolicy = async (event) => {
	if (hasData(event) && hasPolicy(event) && isJSON(event) && !isWhiteListed(event) && !dataPolicyRequest(event)) {
		const functionName = getEnvValue(event, 'APPLY_POLICY_FUNCTION_NAME', 'apply-policy')
		await invoke(functionName, event)
			.then(response => {
				event.object = response.object
				logger.debug('api-middleware:apply-policy:result', { object: event.object })
			})
			.catch(error => {
				event.error = error
				logger.error('api-middleware:apply-policy:error', { object: event.object, error })
			})

	} else {
		logger.debug('api-middleware:apply-policy:skipped')
	}
	return event
}