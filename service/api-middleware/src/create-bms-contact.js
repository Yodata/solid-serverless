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
const path = '/api/contact/create'
const isContactCreateRequest = ({ request }) => (
	request
	&& ((request.method || '').toLowerCase() == POST)
	&& matchPath(request.url, path)
)


/**
 * checks event using event.scope, adds event.isAllowed {boolean}
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
		event.stage = 'response'
		event.response = await createContact(event)
			.then(response => {
				response.status = response.statusCode
				return response
			})
			.catch(error => {
				logger.error(error)
				return {
					status: 500,
					statusCode: 500,
					body: error.message
				}
			})
	}
	return event
}


module.exports = createBmsContact
