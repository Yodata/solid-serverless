const schemaIndex = require('./schema')
const schemaParser = require('json-schema-ref-parser')

async function getSchema (payload) {
	let { object, schema } = payload
	const { topic, type } = object
	if (typeof schema === 'string' && schema.startsWith('http')) {
		schema = await schemaParser.dereference(schema)
	} else if (typeof topic === 'string') {
		if (schemaIndex.has(topic)) {
			schema = schemaIndex.get(topic)
		} else {
			throw new Error(`UNKNOWN_TOPIC:${topic}`)
		}
	} else if (typeof type === 'string') {
		if (schemaIndex.has(type)) {
			schema = schemaIndex.get(type)
		} else {
			throw new Error(`UNKNOWN_TYPE:${type}`)
		}
	}
	if (typeof schema === 'undefined') {
		throw new Error(`SCHEMA_NOT_FOUND:${topic}${type}`)
	}
	return schema
}

module.exports = getSchema
