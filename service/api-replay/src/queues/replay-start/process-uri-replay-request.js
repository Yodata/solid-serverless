// Import required AWS SDK clients and commands for Node.js.
const arc = require('@architect/functions')
const logger = require('@yodata/logger')
const { REPLAY_ITEM_LIMIT, REPLAY_BATCH_SIZE } = require('./service-config')

module.exports = processUriReplayRequest

async function processUriReplayRequest (input) {
	let ITEMS_REPLAYED = 0
	const { target, items, filter } = input
	if (!Array.isArray(items)) {
		throw new Error('Items must be an array')
	}

	const message = {
		type: 'ReplayItemsRequest',
		target,
		filter
	}

	while (items.length > 0 && ITEMS_REPLAYED < REPLAY_ITEM_LIMIT) {
		message.items = items.slice(REPLAY_BATCH_SIZE).map(id => {
			if (id.startsWith(message.target)) {
				return id.substring(message.target.length)
			} else {
				return id
			}
		})
		await publishItems(target, items, filter)
		ITEMS_REPLAYED += message.items.length
		logger.info(`published ${ITEMS_REPLAYED} itmes to ${target}`)
	}
}

async function publishItems (target, items, filter) {
	return arc.queues.publish({
		name: 'replay-items',
		payload: {
			target,
			items,
			filter
		}
	}).then(result => {
		logger.debug('PUBLISHED_ITEMS', {
			target,
			items,
			filter,
			result
		})
		return { target, items, filter, result }
	})
		.catch(error => {
			logger.error('PUBLISH_ITEM_ERROR', { target, items, error })
			return {
				target, items, error: { message: error.message, stack: error.stack }
			}
		})
}
