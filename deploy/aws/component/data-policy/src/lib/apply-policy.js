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
module.exports = async function ApplyDataPolicies(event) {
    try {
        logger.info('apply-data-policies', event)
        let policySet = await getPolicies(event)
        policySet.forEach(policy => {
            let processor = policy.processor
            let policyValue = JSON.parse(policy.value)
            switch(policy.processor) {
                case 'Yodata':
                    event.object = new Transform.Context(policyValue).map(event.object)
                    break
                default:
                    logger.error('unknown data policy.processor', {policy})
            }

        })
    } catch (error) {
        logger.error('error applying data polices', error)
    }
    logger.debug('apply-data-policy:result', event.object)
    return event
}