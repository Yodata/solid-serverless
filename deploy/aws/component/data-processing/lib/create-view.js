const checkScope = require('./check-scope').handler
const applyPolicy = require('./apply-policy').handler
const normalize = require('./normalize').handler
const denormalize = require('./denormalize').handler

/**
 * 
 * creates a view of event.object with scope 
 * and policies applied.
 * 
 * @param {CreateViewEvent} event
 * @returns {CreateViewEvent}
 * 
 * @typedef CreateViewEvent
 * @property {object} object
 * @property {object} event.object   - the data to be transformed
 * @property {object} event.scope    - the ACL.scope
 * @property {object} event.policy   - from {POD}/settings/yodata/policy
 */
exports.handler = async function CreateView(event) {
    return event
}