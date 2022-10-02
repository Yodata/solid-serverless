const arc = require('@architect/functions')
const TARGET_QUEUE = 'replay-queue'

/**
 *
 * @param {object} input
 * @param {string<uri>} input.target - target container url¯
 * @param {object} input.object
 * @param {string[]} input.object.items - array of ids to be replayed from the target container
 * @returns
 */
async function sendBatchToReplqyItemsQueue (input) {
	const params = {
		name: TARGET_QUEUE,
		payload: input
	}
	return arc.queues.publish(params).then(() => {
		return `sent ${input.object.items.length} items to replay from ${input.target}`
	})
}

module.exports = sendBatchToReplqyItemsQueue
