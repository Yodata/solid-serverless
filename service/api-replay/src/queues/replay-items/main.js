const Client = require('@yodata/client')
const { SOLID_HOST, SVC_KEY } = require('./service-config')
const client = new Client({ hostname: SOLID_HOST, hostkey: SVC_KEY })
module.exports = handleReplayItemsEvent

async function handleReplayItemsEvent (event) {
	const pmap = await import('p-map')
	const { target, items } = event

	async function touch (name) {
		const location = client.resolve(target + name)
		return client
			.get(location)
			.then(async response => {
				const { statusCode, data } = response
				if (statusCode === 200 && data && data.id) {
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
	const result = await pmap.default(items, touch, { concurrency: 5 })
	return result
}
