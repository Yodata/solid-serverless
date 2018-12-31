const logger = require('./logger')
const client = require('./solid-client')
const DOMAIN = process.env.DOMAIN
const snakeCase = require('lodash/snakeCase')
const defaults = require('lodash/defaults')



/**
 * saves policy to pod:public/data-policy/{name}
 * @async
 * @param {object} event
 * @param {string} event.name - http uri of the policy
 * @param {object} event.value - the policy value
 * @returns {string} the policy iri
 */
module.exports = async (event) => {
    let policyName = snakeCase(event.name)
    let policyValue = defaults()
    const id = `https://${DOMAIN}/public/data-policy/${policyName}`
}

function isUri(value) {
    return typeof value === 'string' && value.startsWith('http')
}
