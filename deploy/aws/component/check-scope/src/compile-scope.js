// @ts-check

const logger = require('./lib/logger')
const client = require('./lib/solid-client')
const entries = require('lodash/entries')
const {map} = require('p-iteration')

/**
 * returns an iterable set from a scope Map, resolving and fetching remote scopes by uri
 * @async
 * @param {object} event
 * @returns {Promise<Array>}
 */
module.exports = async function compileScope(event) {
	const eventScope = event.scope || {}
	let result = []
	try {
		const scopeSet = entries(eventScope)
		result = await map(scopeSet,([scopeName, value]) => {
			if (isUri(value)) {
				value = client.get(value, {json: true})
					.then(response => {
						return response.body
					})
					.catch(error => {
						logger.error('fetch-remote-scope:error', {scopeName, value, error})
						// deny if remote-scope not available
						return {
							effect: 'Deny',
							condition: {}
						}
					})
			}
			return value
		})
	} catch (error) {
		logger.error('compile-scope:fatal-error', {error})
		throw error
	}
	return result
}

function isUri(value) {
	return typeof value === 'string' && value.startsWith('http')
}
