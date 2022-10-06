const logger = require('@yodata/logger')
const Ajv = require('ajv')
const normalizeInput = require('./normalize-input')
/**
 * validates the payload.object with payload.schema and returns isValid or errors array.
 * @param {object} payload
 * @param {object} payload.object - the object to validate
 * @param {object|string} [payload.schema] - URL to a JSON schema or a JSON schema object
 ¯* @returns {Promise<API_Response>} response
 *
 * @typedef API_Response
 * @property {object} object - the item to be validated
 * @property {object|string} schema - the schma (object) or URI to the schema
 * @property {boolean} [isValid] - true if the payload schema was successfully validated
 * @property {object[]} [errors] - error message
 * @property {string} [error] - error message
 */
exports.handler = async (payload) => {
	const response = {}
	try {
		const { object, schema } = await normalizeInput(payload)
		logger.debug({ object, schema })
		const ajv = new Ajv({ allErrors: true })
		const validate = ajv.compile(schema)
		response.isValid = await validate(object)
		if (!response.isValid) {
			response.error = {
				message: ajv.errorsText(validate.errors),
				items: validate.errors
			}
		}
		logger.info(response)
	} catch (error) {
		response.isValid = false
		response.object = payload
		response.error = {
			message: error.message,
			stack: error.stack
		}
		logger.error(response)
	}
	return response
}
