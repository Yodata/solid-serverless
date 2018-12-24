const logger = require('./logger')
const applyPolicy = require('./apply-policy')
const checkScope = require('./check-scope')
const addHeaders = require('./add-headers')
const createContext = require('./create-context')
const set = require('lodash/set')
const get = require('lodash/get')

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
    logger.debug('api-middleware', event)
    return event
    try {
        event = await createContext(event)
        event = await checkScope(event)
        if (get(event,'response.end', false)) {
            return event
        } else {
            return applyPolicy(event)
        }
    } catch (error) {
        logger.error('api-middleware-error', error)
        return event
    }
};
