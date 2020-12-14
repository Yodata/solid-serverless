
const logger = require('./lib/logger')
const invokeLambdaFunction = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')
const POST = 'post'
const path = '/publish/'
const schemaURLs = require('./schemaByTopic')
const getSchema = ({ topic }) => (schemaURLs[topic])

const matchPath = (str = '', subStr) => str.includes(subStr)
const eventShouldBeProcessed = ({ request, hasData, object }) => (
	request
	&& ((request.method || '').toLowerCase() == POST)
	&& matchPath(request.url, path)
	&& hasData
	&& object
	&& Object.keys(schemaURLs).includes(object.topic)
)

const callBmsTransaction = async event => {
	const stage = getEnvValue(event, 'NODE_ENV', 'staging')
	const functionName = getEnvValue(event, 'BMS_TRANSACTION_FUNCTION_NAME', `${stage}-bms-transaction`)
	return invokeLambdaFunction(functionName, {
		headers: { 'Content-Type': 'application/json' },
		body: event.object
	})
}
/**
 * checks event using event.scope, adds event.isAllowed {boolean}
 * @param {object} 	event
 * @param {string} 	event.stage
 * @param {object}	event.request
 * @param {boolean}	event.hasData
 * @param {object} 	event.object
 * @param {object}	[event.isValid]
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
			event.object = response.errors || response.error || response.message || 'validate-schema:unknown-error-type'
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
		// } else {
		// 	await callBmsTransaction(event)
		// 		.then((response = {}) => {
		// 			event.object = (typeof response.body === 'string') ? JSON.parse(response.body) : response.body
		// 			event.object.actionStatus = 'CompletedActionStatus'
		// 			response.status = response.statusCode
		// 			event.response = response
		// 		})
		// 		.catch(error => {
		// 			event.object = Object.assign(event.object, {
		// 				actionStatus: 'FailedActionStatus',
		// 				error: {
		// 					message: error.message,
		// 					stack: error.stack
		// 				}
		// 			})
		// 			event.response = {
		// 				status: '400',
		// 				statusCode: 400,
		// 				headers: {
		// 					'content-type': 'application/json'
		// 				}
		// 			}
		// 		})
		// }
	}
	logger.debug('api-middleware:schema-validation:result', { event })
	return event
}

module.exports = validateSchema