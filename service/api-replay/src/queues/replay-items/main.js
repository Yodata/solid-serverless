const touch = require('./touch')
const validateInput = require('./validate-input')

async function _main (event) {
	return validateInput(event)
		.then(handleReplayItemsEvent)
}

/**
 *
 * @param {object} request - request
 * @param {string} request.target - container url @example http://name.example.com/inbox/
 * @param {string[]} request.items - list of ids within the container to be replayed
 * @param {object} request.options - SERVICE CONFIG
 * @param {object} [request.filter] - optional filter
 * @returns {Promise} returns a Promise resolved when the event is processed or rejected
 */
async function handleReplayItemsEvent (event) {
	const pMap = await import('p-map')
	const { target, items, filter, options } = event
	const { STOP_REPLAY_ON_ERROR, REPLAY_ITEM_CONCURRENCY } = options

	async function touchItem (pathName) {
		return touch({ target, pathName, filter, options })
	}

	const replayOptions = { concurrency: REPLAY_ITEM_CONCURRENCY, stopOnError: STOP_REPLAY_ON_ERROR }
	return pMap.default(items, touchItem, replayOptions)
}

module.exports = exports = _main
