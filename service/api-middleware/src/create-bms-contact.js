// @ts-nocheck


const logger = require('./lib/logger')
const getEnvValue = require('./lib/get-env-value')
const invokeLambdaFunction = require('./lib/invoke-lambda-function')

const createContact = async (event) => {
	const stage = getEnvValue(event, 'NODE_ENV', 'staging')
	const functionName = getEnvValue(event, 'CREATE_SFDC_CONTACT_FUNCTION_NAME', `${stage}-create-sfdc-contact`)
	return invokeLambdaFunction(functionName, event.object)
}
const matchPath = (url = '', subStr) => new URL(url).pathname == subStr
const POST = 'post'
const path = '/api/contact/create/'
const isContactCreateRequest = (event = {}) => {
	const { request } = event
	return (
		request
		&& ((request.method || '').toLowerCase() === POST)
		&& matchPath(request.url, path)
	)
}


/**
 * calls create-sfdc-cotact service if request is a post to /api/contact/create/
 * @param {object} 	event
 * @param {object} 	event.object
 * @param {object} 	event.scope
 * @param {boolean}	event.isAllowed
 * @param {object}	[event.event]
 * @param {object}	[event.response]
 * @returns {Promise<object>}
 */
async function createBmsContact(event) {
	if (isContactCreateRequest(event)) {
		logger.debug('createbmscontact:received', event)
		await createContact(event)
			.then((response = {}) => {
				event.object = response
				let { actionStatus, error } = response
				let status = (error || actionStatus === 'FailedActionStatus') ? 400 : 201
				event.response = {
					status,
					statusCode: String(status),
					headers: {
						'content-type': 'application/json'
					},
					end: true
				}
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
					end: true
				}
			})
		logger.debug('createbmscontact:result', event)
	}
	return event
}


module.exports = createBmsContact
