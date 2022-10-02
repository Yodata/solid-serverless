// learn more about queue functions here: https://arc.codes/queues
const validateReplayRequest = require('./validate-replay-request')
const tranformReplayRequest = require('./transform-replay-request')
const { processReplayRequest } = require('./process-replay-request')

async function handleReplayRequestAction (event) {
	return validateReplayRequest(event)
		.then(tranformReplayRequest)
		.then(processReplayRequest)
}

module.exports = handleReplayRequestAction
