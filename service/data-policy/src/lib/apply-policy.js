// @ts-check

const Transform = require('@yodata/transform')
const logger = require('./logger')
const getPolicies = require('./get-policies')
const { reduce } = require('p-iteration')
const has = require('./object-has')
const isWhiteListed = require('./data-policy-whitelist')

// event must have a policy to apply (duh)
const hasPolicy = (event) => {
	return has(event, 'policy', v => (typeof v === 'object' && Object.keys(v).length > 0))
}

/**
 * Apply event.policy to event.object
 * @param {object} event
 * @param {object} event.object - the data to be transformed
 * @param {object} event.policy - from pod:settings/yodata/data-policies.json
 * @param {string} event.agent - the event requestor
 * @returns {Promise<object>} - the event with object transformed
 */
module.exports = async function ApplyDataPolicies(event) {
	// if event.agent is data policy service (todo: add whitelist)
	if (
		!hasPolicy(event)
		|| isWhiteListed(event)
	) { return event }

	const policySet = await getPolicies(event)
	event.object = await reduce(policySet, applyPolicy, event.object)
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
