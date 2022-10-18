const arc = require('@architect/functions')
const logger = require('@yodata/logger')
const main = require('./main')

const AGENT = 'replay-start'

async function handleEvent (event) {
	const log = {
		agent: AGENT,
		actionStatus: 'ActiveActionStatus',
		result: ''
	}
	return main(event)
		.then(result => {
			log.actionStatus = 'CompletedActionStatus'
			log.result = result
			log.object = event
			logger.info(log)
			return result
		})
		.catch(error => {
			log.actionStatus = 'FailedActionStatus'
			log.result = error.message
			log.object = event
			log.error = {
				message: error.message,
				stack: error.stack
			}
			logger.error(log)
			return Promise.reject(log)
		})
}
exports.handler = arc.queues.subscribe(handleEvent)
exports.testHandler = handleEvent
