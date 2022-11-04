// @format
import { queues } from '@architect/functions'

export async function publish (request) {
	const { body: event } = request
	await queues.publish({
		name: 'publish2-event',
		payload: event
	})
	return event.id
}

export default publish
