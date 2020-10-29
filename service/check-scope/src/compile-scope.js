// @ts-check

const logger = require('./lib/logger')
const client = require('./lib/solid-client')
const entries = Object.entries
const {map} = require('p-iteration')

/**
 * @param {object} event
 * @param {object} event.scope
 * @returns {Promise<object[]>} array of scope entries
 */
module.exports = async function compileScope(event) {
	const scope = event.scope || {}
	const scopeEntries = entries(scope)
	return map(scopeEntries,processScopeEntry)
}

// eslint-disable-next-line no-unused-vars
const processScopeEntry = async ([key, value]) => {
	return isUri(value) ? fetchRemoteScope(value) : value
}

const fetchRemoteScope = async (uri) => {
	return client.get(uri, {json: true})
		.then(res => {
			return res.body
		})
		.catch(error => {
			logger.error('fetch-remote-scope:error', {uri,error})
			return {
				id: uri,
				error: {
					name: 'FETCH_REMOTE_SCOPE_ERROR',
					message: error.message
				},
				effect: 'Deny',
				condition: {}
			}
		})
}

function isUri(value) {
	return typeof value === 'string' && value.startsWith('http')
}
