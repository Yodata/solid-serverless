const logger = require('./logger')
const client = require('./solid-client')
const fromPairs = require('lodash/fromPairs')

const fetchRemoteValues = async (object) => {
    let entries = Object.entries(object).forEach
}

/**
 * merges event.policy.local,global,default
 * @async
 * @param {object} event
 * @param {object} event.policy
 * @param {object} event.policy.local
 * @param {object} event.policy.global
 * @param {object} event.policy.default
 * @returns {object[]}
 */
module.exports = async function getDataPolicies(event) {
    let result = []
    try {
        let policy = event.policy || {}
        let policyMap = policy && Object.assign({}, policy.local, policy.global, policy.default)
        let policySet = Object.entries(policyMap).map(([policyName, value])=>{
            logger.debug({policyName,value})
            if (typeof value === 'string' && value.startsWith('http')) {
                value = client.get(value,{json:true})
                .then(response => {
                    return response.body
                })
                .catch((error) => {
                    logger.error(`error fetching remote data policy`, {policyName, value, error})
                    return value
                })
            }
            return value
        })
        result = await Promise.all(policySet)
    }
    catch (error) {
        logger.error('error:get-data-policies', {event,error})
        result = []
    }
    return result
}