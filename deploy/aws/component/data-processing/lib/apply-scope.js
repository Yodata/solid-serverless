
/**
 * applies data policies
 * @param {Object} event
 * @param {Object} event.object - the JSON object to be transformed
 * @param {Object} event.policyMap - from {POD}/settings/yodata/policy
 * @returns {Object} - the event with object transformed
 * 
 * @type DataPolicy
 * @property {string} id - URI
 * 
 * @example {YAML}
 * type: DataPolicy
 * processor: JsonSchema
 * object:
 *  additionalProperties: true
 *  properties:
 *      password: { $redact: true } # redact passwords always
 */
async function ApplyDataPolicies(event) {
    if (event.object.hasOwnProperty('password')) {
        event.object.password = '**REDACTED**'
    }
    return event
}

exports.post = ApplyDataPolicies
exports.default = ApplyDataPolicies


 