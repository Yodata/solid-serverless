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
    logger.info('api-middleware', event)
    event = await createContext(event)
    event = await checkScope(event)
    console.log('JSON from event' + JSON.stringify(event))

    if (get(event,'response.end', false)) {
        return event
    } else {
        return applyPolicy(event)
    }
};
