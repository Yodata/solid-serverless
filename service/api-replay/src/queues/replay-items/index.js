const arc = require('@architect/functions')
const logger = require('@yodata/logger')
const main = require('./main')

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
		})
		.catch(error => {
			log.actionStatus = 'FailedActionStatus'
			log.result = error.message
			log.error = {
				message: error.message,
				stack: error.stack
			}
			logger.error(log)
		})
		.finally(() => {
			return log
		})
}
exports.handler = arc.queues.subscribe(handleReplayItemEvent)
exports.testHandler = handleReplayItemEvent
