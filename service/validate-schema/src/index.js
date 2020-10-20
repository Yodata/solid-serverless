const logger = require('@yodata/logger')
const Ajv = require('ajv')
const schemaParser = require('json-schema-ref-parser')
/**
 * What does your function do?
 * @param {object} event
 * @param {object} event.object - the item to be validated
 * @param {object|string} event.schema - a validate json-schema (draft 7)
 * @param {boolean} [event.isValid] - response item
 * @param {string} [event.errors] - response item
 * @param {object} [event.error] - response item
 *
 * @returns {Promise<Response>} response
 *
 * @typedef Response
 * @property {object} object - the item to be validated
 * @property {object|string} schema - the schma (object) or URI to the schema
 * @property {boolean} [isValid] - true if the event schema was successfully validated
 * @property {string} [errors] - error message
 * @property {string} [error] - error message
 */
exports.handler = async (event) => {
	let { object, schema } = event
	try {
		if (typeof schema === 'string' && schema.startsWith('http')) {
			schema = await schemaParser.dereference(schema).then(result => (result.payload ? result.payload : result))
			event.schema = schema
		}
		const ajv = new Ajv()
		const validate = ajv.compile(schema)
		event.isValid = await validate(object)
		if (!event.isValid) {
			event.errors = validate.errors
			event.error = {
				message: ajv.errorsText(validate.errors)
			}
		}
	} catch (error) {
		event.isValid = false
		event.errors = error.message
		event.error = {
			message: error.message,
			stack: error.stack
		}
		logger.error('FailedActionStatus', event)
	}
	logger.debug(event)
	return event
}
