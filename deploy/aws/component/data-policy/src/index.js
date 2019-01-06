const createHandler = require('./lib/create-handler')
const applyPolicy = require('./lib/apply-policy')
const getPolicies = require('./lib/get-policies')
const setPolicy = require('./lib/set-policy')

exports.handler = createHandler(echoHandler, 'echo-handler')
exports.applyPolicy = createHandler(applyPolicy, 'apply-policy')
exports.getPolicies = createHandler(getPolicies, 'get-policies')
exports.setPolicy = createHandler(setPolicy, 'set-policy')

async function echoHandler(event, context) {
	console.log({event, context})
	return event
}
