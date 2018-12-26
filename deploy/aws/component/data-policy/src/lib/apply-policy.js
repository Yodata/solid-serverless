const Transform = require('@yodata/transform')

/**
 * Apply event.policy to event.object
 * @param {Object} event
 * @param {Object} event.object - the data to be transformed
 * @param {Object} event.policy - from pod:settings/yodata/data-policies.json
 * @returns {Object} - the event with object transformed
 */
async function ApplyDataPolicies(event) {

    if (event.object && event.object.hasOwnProperty('password')) {
        event.object.password = '**REDACTED**'
    }
    return event
}

exports.handler = ApplyDataPolicies