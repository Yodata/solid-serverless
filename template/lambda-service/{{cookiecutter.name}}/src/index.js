const logger = require('@yodata/logger')

/**
 * What does your function do?
 * @param {object} event
 * @param {string} event.param - comment
 *
 * Context doc: https://docs.aws.amazon.com/lambda/latest/dg/nodejs-prog-model-context.html
 * @param {Object}   context
 * @param {string}   context.logGroupName - Cloudwatch Log Group name
 * @param {string}   context.logStreamName - Cloudwatch Log stream name.
 * @param {string}   context.functionName - Lambda function name.
 * @param {string}   context.memoryLimitInMB - Function memory.
 * @param {string}   context.functionVersion - Function version identifier.
 * @param {function} context.getRemainingTimeInMillis - Time in milliseconds before function times out.
 * @param {string}   context.awsRequestId - Lambda request ID.
 * @param {string}   context.invokedFunctionArn - Function ARN.
 *
 * @returns {Promise<object>} response
 */
exports.handler = async (event, context) => {
	try {
		logger.debug('{{cookiecutter.name}} received event', {event, context})
	} catch (error) {
		logger.error('{{cookiecutter.name}} failed', {error, context})
	}
	logger.debug('{{cookiecutter.name}} response', event)
	return event
}
