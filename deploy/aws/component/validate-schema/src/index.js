const logger = require('@yodata/solid-serverless-logger').defaultLogger
const Ajv = require('ajv')
/**
 * What does your function do?
 * @param {object} event
 * @param {object} event.object - the item to be validated
 * @param {object} event.schema - a validate json-schema (draft 7)
 *
 * @returns {object} response
 * @returns {boolean} response.isValid
 * @returns {object} response.errors
 */
exports.handler = async (event, context) => {
	try {
		logger.debug('validate-schema received event', {event, context})
		const ajv = new Ajv()
		const validate = ajv.compile(event.schema)
		event.isValid = validate(event.object)
		event.errors = validate.errors
	} catch (error) {
		logger.error('validate-schema failed', {error, context})
		event.isValid = false
		event.errors = error.message
	}
	logger.debug('validate-schema response', event)
	return event
}
