const logger = require('./logger')
const client = require('./solid-client')
const isWhiteListed = require('./data-policy-whitelist.js')

/**
 * Merges event.policy.local,global,default
 * @async
 * @param {object} event
 * @param {object} event.policy
 * @param {object} event.policy.local
 * @param {object} event.policy.global
 * @param {object} event.policy.default
 * @returns {Promise<object[]>}
 */
module.exports = async function getDataPolicies(event) {
	const fetchedPolicies = []
	let result = []
	try {
		const eventPolicy = event.policy || { local: {}, global: {}, default: {} }
		const policyMap = eventPolicy && Object.assign({}, eventPolicy.local, eventPolicy.global, eventPolicy.default)
		delete policyMap.id
		delete policyMap[ '@id' ]
		const policySet = Object.entries(policyMap).map(([ policyName, value ]) => {

			if (isUri(value) && !fetchedPolicies.includes(value) && !isWhiteListed(value)) {
				fetchedPolicies.push(value)
				value = client.get(value, { json: true })
					.then(response => {
						const result = (typeof response.body === 'string') ? JSON.parse(response.body) : response.body
						if (typeof result == 'object' && result.id) delete result.id
						if (typeof result == 'object' && result[ '@id' ]) delete result[ '@id' ]
						return result
					})
					.catch(error => {
						logger.error('data-policy:get-policy:error:fetch-remote-policy', { policyName, value, error })
						return {}
					})
			}
			return value
		})
		result = await Promise.all(policySet)
	} catch (error) {
		logger.error('data-policy:get-policy:error:get-data-policies', { error, object: event })
		result = []
	}
	return result
}

function isUri(value) {
	return (typeof value === 'string' && value.startsWith('http'))
}
