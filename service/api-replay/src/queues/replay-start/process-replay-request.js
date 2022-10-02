// Import required AWS SDK clients and commands for Node.js.
const arc = require('@architect/functions')
const { ListObjectsV2Command } = require('@aws-sdk/client-s3')
const s3Client = require('./s3client.js')
const logger = require('@yodata/logger')
const { REPLAY_ITEM_LIMIT, REPLAY_BATCH_SIZE } = require('./service-config')

function getItemId (item) {
	return item.Key.split('/').slice(-1)[0]
}

function createItemReducer (endPath) {
	return (result, item, index, initialValue) => {
		if (!Array.isArray(result)) result = initialValue
		if (item.Key < endPath) {
			result.push(getItemId(item))
		}
		return result
	}
}

async function publishItems (target, items) {
	arc.queues.publish({
		name: 'replay-items',
		payload: {
			target,
			items
		}
	})
}
/**
 * gets replay list from store (S3 bucket) &
 * publishes replay items commands to replay-items queue (1000 item batches)
 * @function processReplayRequest
 * @param {object} input
 * @param {string} input.target - the container url to be replayed
 * @param {string} input.bucket - the bucket where the replay items are stored
 * @param {string} input.prefix - lowest level path containing all replay items
 * @param {string} input.startPath - the path to start replaying from
 * @param {string} input.endPath - the path to end replaying from
 * !
 */
async function processReplayRequest (input) {
	const { target, bucket, prefix, startPath, endPath } = input
	const reducer = createItemReducer(endPath)

	const commandOptions = {
		Bucket: bucket,
		Prefix: prefix,
		StartAfter: startPath,
		MaxKeys: REPLAY_BATCH_SIZE
	}

	let replayCompleted = false // true when one or more items in response are greater than the endPrefix
	let lastKey = startPath
	let itemsPublished = 0
	let statusMessage = ''

	while (!replayCompleted) {
		try {
			// logger.debug('ListObjectsV2Command', commandOptions)
			const response = await s3Client.send(new ListObjectsV2Command(commandOptions))
			if (Number(response.KeyCount) > 0) {
				const items = response.Contents.reduce(reducer, [])
				await publishItems(target, items)
				itemsPublished += items.length
				lastKey = response.Contents.slice(-1)[0].Key
			}
			// stop if past the endPath, or
			// no more items in the bucket, or
			// no contents (not an array)
			if (lastKey > endPath) {
				replayCompleted = true
				statusMessage = 'stopped after end date'
			}
			if (itemsPublished >= REPLAY_ITEM_LIMIT) {
				replayCompleted = true
				statusMessage = `replay limit (${REPLAY_ITEM_LIMIT}) reached`
			}
			if (!response.IsTruncated) {
				replayCompleted = true
				if (response.KeyCount === 0 && itemsPublished === 0) {
					statusMessage = 'no items found to replay - check your input'
				} else {
					statusMessage = 'end of container reached'
				}
			}
			if (!replayCompleted) {
				commandOptions.StartAfter = lastKey
				logger.debug('REPLAY_STATUS', {
					target,
					itemsPublished,
					lastKey
				})
			}
		} catch (err) {
			replayCompleted = true
			throw new Error(err.message)
		}
	}
	return `queued ${itemsPublished} items to replay on ${target}, ${statusMessage}`
}

exports.processReplayRequest = processReplayRequest
exports.createItemReducer = createItemReducer
exports.getItemId = getItemId
