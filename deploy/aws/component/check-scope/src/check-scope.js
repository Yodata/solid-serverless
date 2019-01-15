// @ts-check

const logger = require('./lib/logger')
const {AuthorizationScope} = require('@yodata/solid-tools')
const compileScope = require('./compile-scope')
const { some } = require('p-iteration')

const checkScope = async (event) => {
	const scope = event.scope
	logger.debug('compiling-scope', {scope})
	const scopeList = await compileScope(event)
	logger.debug('compile-scope:response', scopeList)
	event.isAllowed = await some(scopeList,testScopeEntry(event))
	logger.debug('check-scope:result', {isAllowed: event.isAllowed})
	return event
}

const testScopeEntry = (event) => async (scope, index) => {
	const {object} = event
	const {error} = scope
	if (error) {
		logger.error('scope-error', {error})
		event.error = Array.isArray(event.error) ? event.error.concat(error) : [error]
	}
	const validator = new AuthorizationScope(scope)
	const isAllowed = validator.isAllowed(object)
	logger.debug('test-scope-entry', {index,scope,isAllowed,object})
	return isAllowed
}

module.exports = checkScope