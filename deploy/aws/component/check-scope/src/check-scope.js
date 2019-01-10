const logger = require('./lib/logger')
const {AuthorizationScope} = require('@yodata/solid-tools')
const compileScope = require('./compile-scope')
const { some } = require('p-iteration')

const checkScope = async (event) => {
	const scopeList = await compileScope(event.scope)
	const object = event.object
	logger.debug('check-scope:object', object)
	logger.debug('compile-scope:response', {scopeList})
	event.isAllowed = await some(scopeList,(scope)=> {
		let test = new AuthorizationScope(scope)
		let isAllowed = test.isAllowed(object)
		logger.debug('check-scope:item:result', {scope,isAllowed})
	})
	logger.debug('check-scope:response', event)
	return event
}

module.exports = checkScope