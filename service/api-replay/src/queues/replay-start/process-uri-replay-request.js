// Import required AWS SDK clients and commands for Node.js.
const arc = require('@architect/functions')
const logger = require('@yodata/logger')
const { REPLAY_ITEM_LIMIT, REPLAY_BATCH_SIZE } = require('./service-config')

module.exports = processUriReplayRequest

async function processUriReplayRequest (input) {
	const { target, items, filter } = input
	const response = []
	let ITEMS_REPLAYED = 0

	if (!Array.isArray(items)) {
		throw new Error('Items must be an array')
	}

	const message = {
		type: 'ReplayItemsRequest',
		target,
		filter
	}

	while (items.length > 0 && ITEMS_REPLAYED < REPLAY_ITEM_LIMIT) {
		message.items = items.splice(0, REPLAY_BATCH_SIZE).map(id => {
			if (id.startsWith(message.target)) {
				return id.substring(message.target.length)
			} else {
				return id
			}
		})

		const result = await publishItems(message)
		ITEMS_REPLAYED += message.items.length
		response.push({
			message: 'PUBLISHED_ITEMS',
			itemCount: message.items.length,
			totalItemsPublished: ITEMS_REPLAYED,
			items: message.items,
			result
		})
	}
	return response
}

async function publishItems ({ target, items, filter }) {
	return arc.queues.publish({
		name: 'replay-items',
		payload: {
			target,
			items,
			filter
		}
	})
		.catch(error => {
			logger.error('PUBLISH_ITEM_ERROR', { target, items, error })
			return `ERROR: ${error.message}`
		})
}
