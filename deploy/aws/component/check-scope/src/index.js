const logger = require('@yodata/solid-serverless-logger').defaultLogger
const {AuthorizationScope} = require('@yodata/solid-tools')

/**
 * @typedef CheckScopeResponse
 * @property {object} object - the tested value
 * @property {object} scope - the scope used to test the object
 * @property {boolean} isAllowed - true if allowed
 */

/**
 * validates event.object with event.scope returning event.isAllowed {boolean}
 * @name check-scope
 * @param {object} event
 * @param {object} event.object   - data to be tested
 * @param {object} event.scope    - the ACL.scope value
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
 * @returns {CheckScopeResponse}
 */
exports.handler = async (event, context) => {
    try {
        logger.debug('check-scope recieved event ', event)
        if (!event.scope) {
            logger.error('event.scope undefined', event)
            event.isAllowed = false
        } else {
            let scope = new AuthorizationScope(event.scope)
            event.isAllowed = scope.isAllowed(event.object)
        }
    } catch (error) {
        logger.error('check-scope error', {error, context})
        //TODO: remove this in production
        event.isAllowed = false
    }
    logger.debug('check-scope: event.isAllowed:', event.isAllowed)
    return event
};