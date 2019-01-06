// @ts-check

const logger = require('./lib/logger')
const client = require('./lib/solid-client')

/**
 * returns an iterable set from a scope Map, resolving and fetching remote scopes by uri
 * @async
 * @param {object} event
 * @param {object} event.scope
 * @returns {Promise<Array>}
 */
module.exports = async function compileScope(event) {
	let result = []
	try {
		const eventScope = event.scope || {}
		const scopeSet = Object.entries(eventScope).map(([scopeName, value]) => {
			if (isUri(value)) {
				value = client.get(value, {json: true})
					.then(response => {
						return response.body
					})
					.catch(error => {
						logger.error('error fetching remote scope', {scopeName, value, error})
						return value
					})
			}
			return value
		})
		result = await Promise.all(scopeSet)
	} catch (error) {
		logger.error('error:get-data-scopes', {event, error})
		result = []
	}
	return result
}

function isUri(value) {
	return typeof value === 'string' && value.startsWith('http')
}
