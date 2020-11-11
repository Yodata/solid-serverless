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
// event must have an agent
const hasAgent = (event = {}) => {
	const { agent, instrument } = event
	let subject = agent || instrument
	let result
	if (typeof subject === 'string') {
		result = subject.length > 3
	} else {
		// is this a solid service request?
		result = has(event, 'request.solidService', true)
		const message = result ? 'api-middleware:solid-service-request' : 'api-middleware:no-agent-found'
		logger.debug(message)
	}
	return result
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

	if (!hasAgent(event)) {
		if (has(event, 'request.solidService', true)) {
			logger.debug('SOLID_SERVICE_EVENT')
			event.agent = event.request.url || `https://dps.${SOLID_HOST}/profile/card#me`
			return true
		} else {
			logger.debug('NO event.agent')
			return false
		}
	}
	console.log(`agent=${event.agent}`)
	const agent_host = String(event.agent).startsWith('http') ? new URL(event.agent).hostname : event.agent
	if (agent_host === SOLID_HOST) {
		logger.debug('AGENT_IS_SOLID_HOST')
		return true
	}
	if (agent_host === DATA_POLICY_SVC_HOST) {
		logger.debug('AGENT_IS_DATA_POLICY_SVC_HOST')
		return true
	}
	if (String(DATA_POLICY_WL).length > 0) {
		const whitelist = String(DATA_POLICY_WL).split(',')
		const index = whitelist.findIndex((v) => (String(agent_host).includes(v)))
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
exports.hasAgent = hasAgent


/**
 * Apply data policies
 * @param {object} event
 * @param {object} event.request - the HTTP.Request
 * @param {object} [event.response] - HTTP.Response
 * @param {object} event.object - the data in JSON format to be applied
 * @param {object} [event.policy]
 */
exports.applyDataPolicy = async (event) => {
	if (isProfileReadEvent(event) && hasData(event) && hasPolicy(event) && isJSON(event) && !isWhiteListed(event) && !dataPolicyRequest(event)) {
		const functionName = getEnvValue(event, 'APPLY_POLICY_FUNCTION_NAME', 'apply-policy')
		event.object = await invoke(functionName, event).then(response => response.object)
		logger.debug('apply-policy:result', event.object )
	} else {
		logger.debug('api-middleware:data-policy:skipped')
		return event
	}
}