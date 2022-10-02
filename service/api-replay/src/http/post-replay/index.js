const arc = require('@architect/functions')
const logger = require('@yodata/logger')
const validate = require('./validate-replay-input')

async function publish (event) {
	await arc.queues.publish({
		name: 'replay-start',
		payload: event
	})
	return 'replay request sent successfully'
}

async function handler (req) {
	logger.log('recieved request', req)
	const event = req.body
	const log = {
		agent: 'post-replay',
		object: event
	}
	await validate(event)
		.then(publish)
		.then(result => {
			log.result = result
			log.actionStatus = 'CompletedActionStatus'
			logger.info(log)
		})
		.catch(err => {
			log.result = err.message
			log.actionStatus = 'FailedActionStatus'
			logger.error(log)
		})
	return {
		json: log
	}
}

exports.handler = arc.http.async(handler)
