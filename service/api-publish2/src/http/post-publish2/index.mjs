import { authorize } from './authorize.mjs'
import { addEventMetaData } from './addEventMetaData.mjs'
import { publish } from './publishEvent.mjs'
import { logEvent, logError } from './logEvent.mjs'

export async function handler (req) {
	const request = JSON.parse(req.body)
	request._log = {}
	await authorize(request)
		.then(addEventMetaData)
		.then(publish)
		.then(logEvent(request))
		.then(request => {
			// return HTTP response
			const { body: event } = request
			return {
				statusCode: 204,
				body: JSON.stringify({ id: event.id }, null, 2)
			}
		})
		.catch(logError(request))
}
