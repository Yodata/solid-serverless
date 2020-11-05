// @ts-check

const Transform = require('@yodata/transform')
const logger = require('./logger')
const getPolicies = require('./get-policies')
const { reduce } = require('p-iteration')


/**
 * Apply event.policy to event.object
 * @param {object} event
 * @param {object} event.object - the data to be transformed
 * @param {object} event.policy - from pod:settings/yodata/data-policies.json
 * @returns {Promise<object>} - the event with object transformed
 */
module.exports = async function ApplyDataPolicies(event) {
	// if event.agent is data policy service (todo: add whitelist)
	if (nopolicy(event) || !hasPolicy(event)) { return event }
	else {
		const policySet = await getPolicies(event)
		event.object = await reduce(policySet, applyPolicy, event.object)
	}
	return event
}

const applyPolicy = (object, policy) => {
	const processor = policy.processor || 'Yodata'
	const policyValue = (typeof policy.value === 'string') ? JSON.parse(policy.value) : policy.value
	switch (processor) {
	case 'Yodata':
		object = new Transform.Context(policyValue).map(object)
		break
	default:
		logger.error(`data-policy:unknown-processor:${processor}`)
	}
	return object
}

const hasPolicy = (event) => {
	return event && typeof event.object === 'object' && event.policy && typeof event.policy === 'object' && Object.keys(event.policy).length > 0
}

const nopolicy = event => {
	const WHITELIST = String(process.env.DATA_POLICY_WL).split(',').concat([
		process.env.DATA_POLICY_SVC_HOST,
		process.env.SOLID_HOST,
	])

	try {
		const agent = new URL(event.agent).host
		return WHITELIST.includes(agent)
	} catch (error) {
		logger.error(error.message)
		return false
	}
}