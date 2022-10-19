const touch = require('./touch')
const validateInput = require('./validate-input')

module.exports = async function (event) {
	return validateInput(event)
		.then(handleReplayItemsEvent)
}

/**
 *
 * @param {object} request - request
 * @param {string} request.target - container url @example http://name.example.com/inbox/
 * @param {string[]} request.items - list of ids within the container to be replayed
 * @param {object} [request.filter] - optional filter
 * @param {object} [request.options - { stopOnError: true }
 * @returns {Promise} returns a Promise resolved when the event is processed or rejected
 */
async function handleReplayItemsEvent (event) {
	const { STOP_REPLAY_ON_ERROR, REPLAY_ITEM_CONCURRENCY } = require('./service-config')
	const replayOptions = { concurrency: REPLAY_ITEM_CONCURRENCY, stopOnError: STOP_REPLAY_ON_ERROR }
	const pMap = await (await import('p-map')).default
	const { target, items, filter, options } = event

	async function touchItem (pathName) {
		return touch({ target, pathName, filter, options })
	}

	return pMap(items, touchItem, replayOptions)
}
