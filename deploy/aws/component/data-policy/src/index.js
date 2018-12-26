const logger = require('@yodata/solid-serverless-logger').defaultLogger
const applyPolicy = require('./lib/apply-policy')
const getPolicies = require('./lib/get-policies')

/**
 * what does your function do?
 * @param {object} event
 * @param {string} event.param - comment
 *
 * @returns {Object} response
 * @returns {string} response.param - comment
 */
exports.handler = async (event, context) => {
    try {
        
        logger.debug('data-policy received event', {event,context})
    } catch (error) {
        logger.error('data-policy failed', {error, context})
    }
    logger.debug('data-policy response', event)
    return event
};
