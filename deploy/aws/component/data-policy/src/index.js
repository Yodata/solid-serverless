const logger = require('@yodata/solid-serverless-logger').defaultLogger
const applyPolicy = require('./lib/apply-policy')
const getPolicies = require('./lib/get-policies')

const createHandler = (fn, name) => async (event,context) => {
    try {
        logger.debug(`start:${name}`, {event,context,name})
        event = await fn(event)
    } catch (error) {
        logger.error(`error:${name}`, {error, event, context})
    }
    logger.info(`completed:${name}`, { name , event, context })
    return event
}

exports.handler = createHandler(async (event,context) => event, 'handler')
exports.applyPolicy = createHandler(applyPolicy, 'apply-policy')
exports.getPolicies = createHandler(getPolicies, 'get-policies')