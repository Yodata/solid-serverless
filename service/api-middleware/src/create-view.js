// @ts-check

const logger = require('./lib/logger')
const invoke = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')
const assert = require('assert-plus')

const CREATE_VIEW_FUNCTION_NAME = 'CREATE_VIEW_FUNCTION_NAME'

/**
 * Apply data policies
 * @param {object} event
 * @param {object} event.stage - the event request stage {request|response}
 * @param {object} event.object - the parsed event.request body
 * @param {boolean} event.hasData - true if the event has data
 */
module.exports = async (event) => {
	if (needsToBeProcessed) {
		const lambdaFunction = getEnvValue(event, CREATE_VIEW_FUNCTION_NAME, 'create-view')
		assert.string(lambdaFunction, CREATE_VIEW_FUNCTION_NAME)
		event.object = await invoke(lambdaFunction, event).then(response => response.object)
		logger.debug('create-view:result', event.object)
	} else {
		logger.debug('create-view:skipped')
	}
	return event
}

const needsToBeProcessed = (event) => {
	return isRequest(event) && hasData(event) && hasContext(event)
}

const isRequest = (event) => {
	return event && event.stage === 'request'
}

const hasData = (event) => {
	return (typeof event.hasData !== 'undefined') ? event.hasData : (typeof event.object !== 'undefined')
}

const hasContext = (event = { object: {} }) => {
	const data = event.object
	return data['@context'] && typeof data['@context'] === 'string' && data['@context'].startsWith('http')
}
