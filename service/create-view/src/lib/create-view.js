const {Context, keyOrder, defaultValues} = require('@yodata/transform')
const createView = require('@yodata/transform-plugin-view')

/**
 * creates a view
 * @param {object} event
 * @param {object} event.object - the data to be transformed
 * @param {object} event.context - the context in JSON or YAML format
 */
module.exports = async (event) => {
	const context = new Context(event.context)
		.use(keyOrder)
		.use(defaultValues)
		.use(createView)
	return context.map(event.object)
}
