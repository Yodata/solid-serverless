const { Context, keyOrder, defaultValues } = require('@yodata/transform')
const createView = require('@yodata/transform-plugin-view')
const get = require('lodash/get')
const has = require('lodash/has')
const request = require('./request')
const logger = require('./logger')

/**
 * creates a view
 * @param {object} event
 * @param {object} event.object - event data to be processed
 * @param {object} [event.scope] - scope containing the context
 * @param {object} [event.context] - the context in JSON or YAML format
 */
module.exports = async (event) => {
	if (event.context && event.object) {
		logger.debug('event.context', { context: event.context })
		return processContext(event.context, event.object)
	}
	if (has(event, 'object.topic') && has(event, ['scope', event.object.topic])) {
		const contextHref = get(event, ['scope', event.object.topic])
		const cdef = await request.get(contextHref)
			.then(res => get(res, 'data', {}))
			.catch(error => {
				console.error('error getting cdef', error.message)
			})
		logger.debug('scope.content=', cdef)
		return processContext(cdef, event.object)
	}
	return event.object
}


function processContext(cdef, data) {
	const context = new Context(cdef)
		.use(keyOrder)
		.use(defaultValues)
		.use(createView)
	return context.map(data)
}