const Client = require('@yodata/client')
const logger = require('@yodata/logger')
const JSON_CONTENT_TYPE = 'application/json'

module.exports = handleReplayItemsEvent

async function handleReplayItemsEvent (event) {
	const pmap = await import('p-map')
	const { target, items } = event
	const { SOLID_HOST, SVC_KEY } = require('./service-config')
	const client = new Client({ hostname: SOLID_HOST, hostkey: SVC_KEY })
	const touch = async (name) => {
		const location = target + name
		return client
			.get(location)
			.then(async response => {
				const { statusCode, statusMessage, contentType, data } = response
				if (statusCode === 200 && contentType.includes(JSON_CONTENT_TYPE)) {
					const putResponse = await client.put(location, data, JSON_CONTENT_TYPE)
					return { statusCode: putResponse.statusCode }
				} else {
					logger.error('UNEXPECTED STATUS_CODE: ' + statusCode, { statusCode, statusMessage, location })
					return { statusCode }
				}
			})
			.then(async response => {
				const { statusCode } = response
				return `${name}:${statusCode}`
			})
			.catch(error => {
				return `ERROR:${name}:${error.message}`
			})
	}
	const result = await pmap.default(items, touch, { concurrency: 5 })
	return result
}
