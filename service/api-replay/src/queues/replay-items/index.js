const arc = require('@architect/functions')
const logger = require('@yodata/logger')
const main = require('./main')
const { STOP_REPLAY_ON_ERROR } = require('./service-config')

const AGENT = 'replay-items'

async function handleReplayItemEvent (event) {
	const log = {
		agent: AGENT,
		actionStatus: 'ActiveActionStatus',
		target: event.target
	}
	return main(event)
		.then(result => {
			log.actionStatus = 'CompletedActionStatus'
			log.result = result
			logger.info(log)
			return log
		})
		.catch(error => {
			log.actionStatus = 'FailedActionStatus'
			log.result = error.message
			log.error = {
				message: error.message,
				stack: error.stack
			}
			log.object = event
			logger.error(log)
			return (STOP_REPLAY_ON_ERROR) ? Promise.reject(log) : Promise.resolve(log)
		})
}
exports.handler = arc.queues.subscribe(handleReplayItemEvent)
exports.testHandler = handleReplayItemEvent
