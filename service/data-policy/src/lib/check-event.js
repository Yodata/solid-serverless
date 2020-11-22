// @ts-check

const logger = require('./logger')
const has = require('./object-has')
const get = require('get-value')
const isWhiteListed = require('./data-policy-whitelist')

const APPEND_ACCESS = 'Append'
const OUTBOX_PATH = '/outbox/'
const PROFILE_PATH = '/profile/card'
const READ_ACCESS = 'Read'
const WRITE_ACCESS = 'Write'


/**
 * @typedef CheckEventResponse
 * @property {string} object - id of the event or object
 * @property {object} [result] - the result
 * @property {string} result.message = reponse/error message
 * @property {boolean} result.policyExecutionRequired	 - true if policy execution is necessary
 * @property {object}  [error]
 * @property {string}  error.message
 * @property {*} error.stack
 *
 */


// event must have a policy to apply (duh)
const hasPolicy = (event) => {
	return has(event, 'policy', v => (typeof v === 'object' && Object.keys(v).length > 0))
}

const hasAgent = event => (typeof event.agent === 'string' && event.agent.startsWith('http'))



/**
 * Apply event.policy to event.object
 * @param {object} event - the api middleware event
 * @param {object} event.object - the data to be transformed
 * @param {object} event.policy - from pod:settings/yodata/data-policies.json
 * @param {string} event.agent - the event requestor
 * @returns {CheckEventResponse}
 */
function checkEvent(event) {
	// no policy no work
	if (!hasPolicy(event)) return allClear('check-event:no-policy-found', event)

	if (!hasAgent(event)) return policyRequired('check-event:no-agent', event)

	if (isWhiteListed(event)) return allClear(`check-event:is-white-listed:${event.agent}`, event)

	const { path, accessType } = get(event, 'request.target', { default: {} })

	// test events (todo: formalize this pattern so we don't need to handcode)

	if (accessType === READ_ACCESS && String(path) == PROFILE_PATH) {
		return policyRequired('check-policy:profile-read', event)
	}

	if ((accessType == WRITE_ACCESS || accessType == APPEND_ACCESS) && String(path).startsWith(OUTBOX_PATH)) {
		return policyRequired(`check-policy:outbox:${String(accessType).toLowerCase()}`, event)
	}

	return allClear('check-event:no-matches', event)

}


/**
 * returns a check event response
 * @param {string} message
 * @param {object} event
 * @returns {CheckEventResponse}
 */
const policyRequired = (message, event) => {
	const { id } = event
	logger.info(`${message}:${id}`, event)
	return {
		object: id,
		result: {
			message,
			policyExecutionRequired: true
		}
	}
}

/**
 * returns a check event response
 * @param {string} message
 * @param {object} event
 * @returns {CheckEventResponse}
 */
const allClear = (message, event) => {
	const response = policyRequired(message, event)
	response.result.policyExecutionRequired = false
	return response
}


module.exports = {
	allClear,
	checkEvent,
	hasAgent,
	hasPolicy,
	policyRequired
}