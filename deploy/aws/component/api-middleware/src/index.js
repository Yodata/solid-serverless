const logger = require('./logger')
const normalizeRequestObject = require('./normalize-request-object')
const checkScope = require('./check-scope')
const applyPolicy = require('./apply-policy')

/**
 * @param {object} event
 * @param {object} event.request - http.Request
 * @param {object} event.response - http.Response
 * @param {string} [event.agent] - the user / actor making the request
 * @param {string} [event.instrument] - application/service or whatever used by the agent to produce the request
 * @param {object} [event.scope] - Authorization.scope
 * @param {object} [event.policy] - 
 * @param {object} [event.policy.local]
 */
exports.handler = async (event) => {
    logger.debug('api-middleware:received-event', event)
    try {
        event = await normalizeRequestObject(event)
        event = await checkScope(event)

        if (event.isAllowed === true) {
            event = await applyPolicy(event)
        }

        logger.debug('api-middleware:response', event)

    } catch (error) {
        logger.error('api-middleware:error', error)
    }
    return event
}

exports.exampleEvent = () => require('./example/event.json')
exports.exampleResponse = () => require('./example/response.json')
exports.exampleErrorEvent = () => require('./example/error-event.json')
exports.exampleErrorResponse = () => require('./example/error-response.json')
