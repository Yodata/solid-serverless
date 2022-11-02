const arc = require('@architect/functions')
const logger = require('@yodata/logger')

const AGENT = 'replay-items'

async function handleReplayItemEvent (event) {
	const main = require('./main')
	const DEFAULT_CONFIG = require('./service-config')
	const getOptions = (event) => Object.assign({}, DEFAULT_CONFIG, event.options)
	event.options = getOptions(event)
	const { STOP_REPLAY_ON_ERROR } = event.options
	const log = {
		agent: AGENT,
		target: event.target,
		options: Object.assign({}, event.options, { SVC_KEY: 'REDACTED' }),
		actionStatus: 'ActiveActionStatus'
	}
	return main(event)
		.then(result => {
			log.actionStatus = 'CompletedActionStatus'
			log.result = result
			log.itemsCount = event.items.lenth
			return log
		})
		.catch(error => {
			log.actionStatus = 'FailedActionStatus'
			log.result = error.message
			log.error = {
				message: error.message,
				stack: error.stack
			}
			if (error.errors) {
				log.errors = error.errors
				log.error.stack = error.errors.join('\n')
			}
			log.object = event
			if (STOP_REPLAY_ON_ERROR) {
				return Promise.reject(log)
			} else {
				logger.error(log)
				return Promise.resolve(log)
			}
		})
}

exports.handler = arc.queues.subscribe(handleReplayItemEvent)
exports.test = handleReplayItemEvent
