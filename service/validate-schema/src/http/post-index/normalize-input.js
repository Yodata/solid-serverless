const getSchema = require('./get-schema')

async function normalizeInput (input) {
	let result = {}
	if (typeof input?.body === 'string') {
		result = JSON.parse(input.body)
	}
	if (typeof result?.object !== 'object') {
		result = {
			object: { ...result }
		}
	}
	result.schema = await getSchema(result)
	return result
}

module.exports = normalizeInput
