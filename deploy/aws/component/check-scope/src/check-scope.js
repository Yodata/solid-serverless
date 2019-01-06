const logger = require('./lib/logger')
const {AuthorizationScope} = require('@yodata/solid-tools')
const compileScope = require('./compile-scope')
const { some } = require('p-iteration')

const checkScope = async (event) => {
	const scopes = await compileScope(event)
	logger.debug('compileScope(event)=', {event,scopes})
	event.isAllowed = await some(scopes,(scope)=> {
		return new AuthorizationScope(scope).isAllowed(event.object)
	})
	return event
}

module.exports = checkScope