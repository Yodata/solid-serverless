// @ts-check

const logger = require('./lib/logger')
const client = require('./lib/solid-client')
const entries = require('lodash/entries')
const {map} = require('p-iteration')

module.exports = async function compileScope(event) {
	const scope = event.scope || {}
	const scopeEntries = entries(scope)
	return map(scopeEntries,processScopeEntry)
}

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
			return {}
		})
}

function isUri(value) {
	return typeof value === 'string' && value.startsWith('http')
}
