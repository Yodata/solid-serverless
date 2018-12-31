const logger = require('./logger')
const getPolicies = require('./get-policies')
const Transform = require('@yodata/transform')

/**
 * Apply event.policy to event.object
 * @param {Object} event
 * @param {Object} event.object - the data to be transformed
 * @param {Object} event.policy - from pod:settings/yodata/data-policies.json
 * @returns {Object} - the event with object transformed
 */
module.exports = async function ApplyDataPolicies(event, context) {
    let object = event.object
    let result = event.object
    try {
        let policySet = await getPolicies(event)
        policySet.forEach(policy => {result = applyPolicy(result,policy)})
    } catch (error) {
        logger.error('error applying data polices', {error, event, context})
    }
    logger.info('apply-data-policy:result', {object,result})
    event.object = result
    return event
}

const applyPolicy = (object,policy) => {
    let processor = policy.processor;
    let policyValue = JSON.parse(policy.value);
    switch (processor) {
        case 'Yodata':
            object = new Transform.Context(policyValue).map(object);
            break;
        default:
            logger.error(`data-policy:unknown-processor:${processor}`);
    }
    return object
};