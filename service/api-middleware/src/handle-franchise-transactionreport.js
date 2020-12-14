
const logger = require('@yodata/logger')
const invokeLambdaFunction = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')
const POST = 'post'
const PUBLISH_PATH = '/publish/'
const TRANSACTION_REPORT = 'realestate/franchise#transactionreport'

const eventShouldBeProcessed = ({ request, hasData, object, isValid }) => (
	request
	&& ((request.method || '').toLowerCase() == POST)
	&& String(request.url).includes(PUBLISH_PATH)
	&& hasData
	&& object
	&& object.topic === TRANSACTION_REPORT
	&& isValid
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
 * @typedef HandleFranchiseTransactionReportResponse
 * @property {object}   event.request - http.request
 * @property {string} 	event.stage - 'request' | 'response'
 * @property {boolean}	event.hasData - true if contentType is serializable
 * @property {object} 	event.object - request.body (data)
 * @property {boolean}  event.isValid - true if event schema is validated
 * @property {object}  event.response - true if event schema is validated
 */


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
 * @returns {Promise<HandleFranchiseTransactionReportResponse>}
 */
async function handleFranchiseTransactionReport(event) {
	if (eventShouldBeProcessed(event)) {
		logger.debug('invoking-bms-transaction', { event })
		await callBmsTransaction(event)
			.then((response = {}) => {
				event.object = (typeof response.body === 'string') ? JSON.parse(response.body) : response.body
				event.object.actionStatus = 'CompletedActionStatus'
				response.status = response.statusCode
				event.response = response
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
					status: '400',
					statusCode: 400,
					headers: {
						'content-type': 'application/json'
					}
				}
			})
	}
	logger.debug('franchise:transactionreport:response', { event })
	return event
}

module.exports = handleFranchiseTransactionReport