const Client = require('@yodata/client')
const checkFilter = require('./check-filter')
const { SOLID_HOST, SVC_KEY, REPLAY_FILTERING_ENABLED, STOP_REPLAY_ON_ERROR } = require('./service-config')
const client = new Client({ hostname: SOLID_HOST, hostkey: SVC_KEY })

const getReplayConfig = (overrides) => {
	const defaultConfig = Object.create({
		stopOnError: STOP_REPLAY_ON_ERROR
	})
	return Object.assign(defaultConfig, overrides)
}

/**
 *
 * @param {object} params - touch request params
 * @param {string} params.target - target container @example https://example.com/inbox/
 * @param {string} params.pathName - container item id @example message+id
 * @param {string} params.filter - see README.md for details
 * @param {string} params.options - touch request options
 * @returns
 */
module.exports = async function _touch ({ target, pathName, filter, options }) {
	const config = getReplayConfig(options)
	const name = pathName
	const location = client.resolve(target + name)
	return client
		.get(location)
		.then(async response => {
			const { statusCode, data } = response
			if (statusCode === 200 && data && data.id) {
				if (filter && REPLAY_FILTERING_ENABLED === '1' && !checkFilter(filter, data)) {
					return `${name}:${statusCode}:DOES_NOT_MATCH_FILTER`
				}
				// items matches the filter so replay it.
				return client.put(location, data).then(response => {
					return `${name}:${response.statusCode}`
				}).catch(error => {
					const statusCode = error.statuCode || 500
					const statusMessage = error.statusMessage || error.message || 'FAILED_TO_PUT_ITEM'
					const message = `${name}:${statusCode}:${statusMessage}`
					const errorResponse = new Error(message)
					Object.assign(errorResponse, { statusCode, statusMessage, url: location })
					throw errorResponse
				})
			} else {
				// failed to get the item
				const getRequestError = new Error('FAILED_TO_GET_ITEM')
				getRequestError.statusCode = statusCode || 500
				getRequestError.statusMessage = response.statusMessage || getRequestError.message
				getRequestError.url = response.url || location
				throw getRequestError
			}
		})
		.catch(error => {
			error.statusCode = error.statusCode || 500
			error.statusMessage = error.statusMessage || error.message
			error.url = error.url || target + pathName
			if (config.stopOnError === true) {
				throw error
			} else {
				return `{pathname}:ERROR:${error.statusCode}:${error.message}`
			}
		})
}
