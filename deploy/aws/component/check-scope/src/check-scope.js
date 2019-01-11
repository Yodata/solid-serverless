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
	const object = event.object
	if (scope.error) {
		logger.error('scope-error', {error: scope.error})
		event.error = Array.isArray(event.error) ? event.error.concat(scope.error) : [scope.error]
	}
	const validator = new AuthorizationScope(scope)
	const isAllowed = validator.isAllowed(object)
	logger.debug('test-scope-entry', {index,scope,isAllowed,error: scope.error})
	return isAllowed
}

module.exports = checkScope