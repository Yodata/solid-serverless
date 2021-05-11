
const logger = require('@yodata/logger')
const invokeLambdaFunction = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')
const POST = 'post'
const PUBLISH_PATH = '/publish/'

const eventShouldBeProcessed = ({ request, hasData, object, isValid }) => (
	request
	&& ((request.method || '').toLowerCase() == POST)
	&& String(request.url).includes(PUBLISH_PATH)
	&& hasData
	&& object
	&& isValid
)

const addSameAsLambda = async event => {
	const stage = getEnvValue(event, 'NODE_ENV', 'staging')
	const functionName = getEnvValue(event, 'ADD_SAMEAS_FUNCTION_NAME', `${stage}-bms-transaction`)
	return invokeLambdaFunction(functionName, {
		headers: { 'Content-Type': 'application/json' },
		body: event.object
	})
}

/**
 *
 * @param {object}  event
 * @param {object}  event.request - http.request
 * @param {string}  event.stage - 'request' | 'response'
 * @param {boolean} event.hasData - true if contentType is serializable
 * @param {object} 	event.object - parsed request/response body
 * @param {boolean} event.isValid - true if event schema is validated
 * @param {object}  event.response - true if event schema is validated
 *
 */
async function addSameAsEventHandler(event) {
	if (eventShouldBeProcessed(event)) {
		logger.debug('invoking-add-sameAs', { event })
		await addSameAsLambda(event)
			.then((response = {}) => {
				logger.debug('add-sameAs-result', { response })
				let object = (typeof response.body === 'string') ? JSON.parse(response.body) : response.body
				event.object = Object.assign(object, {
					actionStatus: object.actionStatus || ((response.status < 300) ? 'CompletedActionStatus' : 'FailedActionStatus')
				})
				let status = response.status || response.statusCode || 201
				event.response = Object.assign(response, {
					status: Number(status),
					statusCode: String(status),
					end: true
				})
			})
			.catch(error => {
				event.object = Object.assign(event.object, {
					actionStatus: 'FailedActionStatus',
					error: {
						message: error.message,
						stack: error.stack
					}
				})
				event.response = {
					status: 400,
					statusCode: '400',
					end: true,
					headers: {
						'content-type': 'application/json'
					}
				}
			})
	}
	logger.debug('add-sameAs-event:response', { event })
	return event
}

module.exports = addSameAsEventHandler