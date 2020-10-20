// @ts-nocheck


const logger = require('./lib/logger')
const getEnvValue = require('./lib/get-env-value')
const invokeLambdaFunction = require('./lib/invoke-lambda-function')

const createContact = async (event) => {
	const stage = getEnvValue(event, 'NODE_ENV', 'staging')
	const functionName = getEnvValue(event, 'CREATE_SFDC_CONTACT_FUNCTION_NAME', `${stage}-create-sfdc-contact`)
	return invokeLambdaFunction(functionName, {
		headers: { 'Content-Type': 'application/json' },
		body: event.object
	})
}
const matchPath = (url = '', subStr) => new URL(url).pathname == subStr
const POST = 'post'
const path = '/api/contact/create/'
const isContactCreateRequest = ({ request }) => (
	request
	&& ((request.method || '').toLowerCase() === POST)
	&& matchPath(request.url, path)
)


/**
 * calls create-sfdc-cotact service if request is a post to /
 * @param {object} 	event
 * @param {object} 	event.object
 * @param {object} 	event.scope
 * @param {boolean}	event.isAllowed
 * @param {object}	[event.event]
 * @param {object}	[event.response]
 * @returns {Promise<object>}
 */
async function createBmsContact(event) {
	logger.debug('createbmscontact:received', event)
	if (isContactCreateRequest(event)) {
		event.stage = 'response'

		await createContact(event)
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
					status: '500',
					statusCode: 500,
					headers: {
						'content-type': 'application/json'
					}
				}
			})
	}
	logger.debug('createbmscontact:result', event)
	return event
}


module.exports = createBmsContact
