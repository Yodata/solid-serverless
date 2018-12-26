const kindOf = require('kind-of')
/**
 * extract and expand event.policy
 * @param {object} event
 * @param {object} event.policy
 * @param {object} event.policy.local
 * @param {object} event.policy.global
 * @param {object} event.policy.default
 * @returns {Promise<array>}
 */
module.exports = async function getDataPolicies(event) {
    let result = []
    let policy = event.policy
    if (kindOf(policy.local) === 'object') {
        Object.entries(policy.local).forEach(([k,v]) => {
            result.push(v)
        })
    }
    if (kindOf(policy.global) === 'object') {
        Object.entries(policy.global).forEach(([k,v]) => {
            result.push(v)
        })
    }
    if (kindOf(policy.default) === 'object') {
        Object.entries(policy.default).forEach(([k,v]) => {
            result.push(v)
        })
    }
    return result
}