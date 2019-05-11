// @ts-check

const logger = require('./lib/logger')
const invoke = require('./lib/invoke-lambda-function')
const getEnvValue = require('./lib/get-env-value')
const assert = require('assert-plus')

const CREATE_VIEW_FUNCTION_NAME = 'CREATE_VIEW_FUNCTION_NAME'

/**
 * process event.object with context if available
 * 
 * @param {object} event
 * @param {object} event.stage - the event request stage {request|response}
 * @param {object} event.object - the parsed event.request body
 * @param {boolean} event.hasData - true if the event has data
 */
module.exports = async (event) => {
	if (needsToBeProcessed) {
		const lambdaFunction = getEnvValue(event, CREATE_VIEW_FUNCTION_NAME, 'create-view')
		assert.string(lambdaFunction, CREATE_VIEW_FUNCTION_NAME)
		event.object = await invoke(lambdaFunction, {
			context: getContext(event),
			object: event.object
		})
		logger.debug('process-context:result', event.object)
	} else {
		logger.debug('process-context:skipped')
	}
	return event
}

const getContext = (event) => {
	return event.object['@context']
}

const getData = (event) => {
	return event.object
}

const needsToBeProcessed = (event) => {
	return isRequest(event) && hasData(event) && hasContext(event) && isPost(event)
}

const isRequest = (event) => {
	return event && event.stage === 'request'
}

const isPost = (event) => {
	return event && event.request && event.request.method === 'POST'
}

const hasData = (event) => {
	return (typeof event.hasData !== 'undefined') ? event.hasData : (typeof event.object !== 'undefined')
}

const hasContext = (event = { object: {} }) => {
	const context = event && event.object && event.object['@context']
	return (typeof context !== null && typeof context !== 'undefined')
}
