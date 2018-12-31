const createHandler = require('./lib/create-handler')
const applyPolicy = require('./lib/apply-policy')
const getPolicies = require('./lib/get-policies')

exports.handler = createHandler(echoHandler, 'echo-handler')
exports.applyPolicy = createHandler(applyPolicy, 'apply-policy')
exports.getPolicies = createHandler(getPolicies, 'get-policies')


async function echoHandler(event,context) {
    console.log({event,context})
    return event
}