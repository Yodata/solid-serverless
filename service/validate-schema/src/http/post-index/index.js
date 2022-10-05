const logger = require('@yodata/logger')
const Ajv = require('ajv')
const schemaParser = require('json-schema-ref-parser')
/**
 * What does your function do?
 * @param {object} event
 * @param {object} event.object - the item to be validated
 * @param {object|string} event.schema - the json schema validation definition
 * @param {boolean} [event.isValid] - true when event is valid
 * @param {object[]} [event.errors] - response item
 * @param {object} [event.error] - response item
 *
 * @returns {Promise<Response>} response
 *
 * @typedef Response
 * @property {object} object - the item to be validated
 * @property {object|string} schema - the schma (object) or URI to the schema
 * @property {boolean} [isValid] - true if the event schema was successfully validated
 * @property {object[]} [errors] - error message
 * @property {string} [error] - error message
 */
exports.handler = async (event) => {
	let { object, schema } = event
	try {
		if (typeof schema === 'string' && schema.startsWith('http')) {
			schema = await schemaParser.dereference(schema)
				.then(dereferencedSchema => {
					if (typeof dereferencedSchema === 'object' && typeof dereferencedSchema?.payload === 'object') {
						return dereferencedSchema.payload
					} else {
						return dereferencedSchema
					}
				})
			event.schema = schema
		}
		const ajv = new Ajv({ allErrors: true })
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
		event.errors = [
			{

				message: error.message
			}
		]
		event.error = {
			message: error.message,
			stack: error.stack
		}
		logger.error('FailedActionStatus', event)
	}
	logger.debug(event)
	return event
}
