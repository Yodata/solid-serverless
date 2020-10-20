
const logger = require('./lib/logger')
const invokeLambdaFunction = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')
const POST = 'post'
const path = '/publish/'
const schemaURLs = require('./schemaByTopic')
const getSchema = ({ topic }) => (schemaURLs[ topic ])

const matchPath = (str = '', subStr) => str.includes(subStr)
const eventShouldBeProcessed = ({ request, hasData, object }) => (
	request
	&& ((request.method || '').toLowerCase() == POST)
	&& matchPath(request.url, path)
	&& hasData
	&& object
	&& Object.keys(schemaURLs).includes(object.topic)
)
/**
 * checks event using event.scope, adds event.isAllowed {boolean}
 * @param {object} 	event
 * @param {object} 	event.object
 * @param {object} 	event.scope
 * @param {boolean}	event.isAllowed
 * @param {boolean}	event.hasData
 * @param {object}	[event.request]
 * @param {object}	[event.response]
 * @returns {Promise<object>}
 */

async function validateSchema(event) {
	if (eventShouldBeProcessed(event)) {
		logger.debug('api-middleware:validate-schema:received', { event })
		const stage = getEnvValue(event, 'NODE_ENV', 'staging')
		const functionName = getEnvValue(event, 'VALIDATE_SCHEMA_FUNCTION_NAME', `${stage}-validate-schema`)
		const props = {
			object: event.object,
			schema: getSchema(event.object)
		}
		let response = await invokeLambdaFunction(functionName, props)
		if (!response.isValid) {
			event.object = response.errors || response.error || response.message || 'oh shit'
			event.response = {
				status: '400',
				headers: {
					'Content-Type': 'application/json'
				},
				statusCode: 400,
				end: true,
				body: Buffer.from(JSON.stringify(event.object)).toString('base64')
			}
		}
	}
	logger.debug('api-middleware:schema-validation:result', { event })
	return event
}

module.exports = validateSchema