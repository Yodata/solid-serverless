const {AuthorizationScope} = require('@yodata/solid-tools')
const ow = require('ow')
const logger = require('./logger')

/**
 * Test scope on a request
 * @name check-scope
 * @param {object} event
 * @param {object} event.request - http.request
 * @param {URI}    event.agent
 * @param {URI}    event.instrument
 * @param {object} event.object   - the data to be transformed
 * @param {object} event.scope    - the ACL.scope
 * @param {object} event.policy   - from {POD}/settings/yodata/policy
 * @async
 */
exports.handler = async event => {
	logger.debug('check-scope', event)
	// Ow(event, ow.object.hasKeys(['object','scope']))
	const scope = new AuthorizationScope(event.scope)
	event.scopeIsAllowed = scope.isAllowed(event.object)
	return event
}
