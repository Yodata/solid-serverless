const { getContext, plugin, mapAsync } = require('@yodata/transform')
const pluginView = require('@yodata/transform-plugin-view')
const get = require('lodash/get')
const has = require('lodash/has')

/**
 * creates a view
 * @param {object} event
 * @param {object} event.object - event data to be processed
 * @param {object} [event.scope] - scope containing the context
 * @param {object} [event.context] - the context in JSON or YAML format
 */
module.exports = async (event) => {
	if (event.context && event.object) {
		const context = await getContext(event.context)
		return processContext(context, event.object)
	}
	if (has(event, 'object.topic') && has(event, [ 'scope', event.object.topic ])) {
		const context = await getContext(get(event, [ 'scope', event.object.topic ]))
		return processContext(context, event.object)
	}
	return event.object
}

/** */
async function processContext(cdef, data) {
	const context = await getContext(cdef)
	context
		.use(plugin.defaultValues)
		.use(plugin.keyOrder)
		.use(pluginView)
		.use(plugin.fetchJsonValue)
	return mapAsync(context)(data)
}