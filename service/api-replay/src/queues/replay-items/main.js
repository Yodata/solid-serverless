const Client = require('@yodata/client')
const checkFilter = require('./check-filter')

module.exports = handleReplayItemsEvent

async function handleReplayItemsEvent (event) {
	const { SOLID_HOST, SVC_KEY, REPLAY_BATCH_SIZE, REPLAY_FILTERING_ENABLED } = require('./service-config')
	const client = new Client({ hostname: SOLID_HOST, hostkey: SVC_KEY })
	const pMap = await (await import('p-map')).default
	const { target, items, filter } = event

	if (Array.isArray(items) && items.Length > REPLAY_BATCH_SIZE) {
		throw new Error('ITEM_COUNT_EXCEEDS_REPLAY_BATCH_SIZE' + items.length)
	}

	async function touch (name) {
		const location = client.resolve(target + name)
		return client
			.get(location)
			.then(async response => {
				const { statusCode, data } = response
				if (statusCode === 200 && data && data.id) {
					if (filter && REPLAY_FILTERING_ENABLED === '1') {
						const matchesFilter = checkFilter(filter, data)
						if (matchesFilter === false) {
							return `${name}:DOES_NOT_MATCH_FILTER`
						}
					}
					// items matches the filter so replay it.
					return client.put(location, data).then(response => {
						return `${name}:${response.statusCode}`
					})
				} else {
					return `${name}:UNEXPECTED_STATUS_CODE:${statusCode}`
				}
			})
			.catch(error => {
				const message = error.statusCode || error.message || 500
				return `${name}:${message}`
			})
	}
	const result = await pMap(items, touch, { concurrency: 5 })
	return result
}
