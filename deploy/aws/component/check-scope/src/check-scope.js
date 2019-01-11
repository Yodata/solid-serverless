const logger = require('./lib/logger')
const {AuthorizationScope} = require('@yodata/solid-tools')
const compileScope = require('./compile-scope')
const { some } = require('p-iteration')

const checkScope = async (event) => {
	const scopeList = await compileScope(event.scope)
	logger.debug('compile-scope:response', {scopeList})
	event.isAllowed = await some(scopeList,testScopeEntry(event.object))
	logger.debug('check-scope:result', {isAllowed: event.isAllowed})
	return event
}

const testScopeEntry = (object) => async (scope) => {
	const validator = new AuthorizationScope(scope)
	const isAllowed = validator.isAllowed(object)
	logger.debug('test-scope-entry:result', {object,scope,isAllowed})
	return isAllowed
}

module.exports = checkScope