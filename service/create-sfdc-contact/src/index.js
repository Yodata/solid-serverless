const logger = require('@yodata/logger')



/**
 * What does your function do?
 * @param {object} event
 * @param {"CreateAction"} event.type
 * @param	{object} event.object the agent to be added
 * @param {string} event.givenName the first name of the agent
 * @param {string} event.familyName the last name of the agent
 * @param {string} event.email email address
 * @param {string} event.telephone telephone
 * @param {string} event.branchCode hsf office id i.e. CA301-001
 * @param {string} event.roleName Full Time Sales Professional or Part Time Sales Professional
 * @param {string} event.jobTitle Sales Associate
 * @param {string} event.bmsID
 * @param {string} [event.additionalName] the middle name or nickname of the agent
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
 * @returns {Promise<object>}
 */
exports.handler = async (event, context) => {
	try {
		logger.debug('create-sfdc-contact received event', {event, context})
	} catch (error) {
		logger.error('create-sfdc-contact failed', {error, context})
	}
	logger.debug('create-sfdc-contact response', event)
	return event
}
