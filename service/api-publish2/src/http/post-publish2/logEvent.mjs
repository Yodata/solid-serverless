// @format
import * as logger from '@yodata/logger'

export function logEvent (request) {
	return async (result) => {
		const { body: event, _log: log = {} } = request
		const { agent, instrument, time, id, topic } = event
		Object.assign(
			log,
			{
				time,
				id,
				topic,
				agent,
				instrument,
				result
			})
		logger.log(log)
		return request
	}
}

export const logError = (request) => {
	return async (error) => {
		const { body: event, _log: log = {} } = request
		log.error = {
			message: error.message,
			stack: error.stack
		}
		log.object = event
		log.result = `ERROR:${error.message}:${event.id}`
		logger.error(log)
		return Promise.reject(log.result)
	}
}

export default logEvent
