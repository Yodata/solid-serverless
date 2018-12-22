
/**
 * returns event.object after transformation 
 * @param {Object} event 
 * @param {Object} event.object - the value to be transformed
 * @param {Object} event.scope - an ACL.scope
 * @param {Object} event.policy - from /settings/yodata/policy.json
 * @returns {Object} the event.object after transformation
**/
exports.post = async (event) => {
    return event.object
}