const { keyOrder, defaultValues, getContext } = require('@yodata/transform')
const pluginView = require('@yodata/transform-plugin-view')
const get = require('lodash/get')
const has = require('lodash/has')
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
		logger.info('found event.context', event)
		return processContext(event.context, event.object)
	}
	if (has(event, 'object.topic') && has(event, ['scope', event.object.topic])) {
		logger.info(`found topic (${event.object.topic}) and scope (${event.scope[event.object.topic]})`)
		const contextHref = get(event, ['scope', event.object.topic])
		return processContext(contextHref, event.object)
	}
	return event.object
}

async function processContext(cdef, data) {
	const context = await getContext(cdef)
	context
		.use(keyOrder)
		.use(defaultValues)
		.use(pluginView)

	return context.map(data)
}