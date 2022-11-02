const requiredEnvironmentVariables = [
	'SOLID_HOST',
	'SVC_KEY',
	'REPLAY_BATCH_SIZE',
	'REPLAY_FILTERING_ENABLED',
	'REPLAY_ITEM_CONCURRENCY',
	'STOP_REPLAY_ON_ERROR'
]

module.exports = async function _validateInput (event) {
	const errors = []
	if (!event.target) errors.push('event.target is required')
	if (!event.options) errors.push('event.options is required')
	requiredEnvironmentVariables.forEach(key => {
		if (typeof !event?.options?.[key] === 'undefined') errors.push(`missing required environment variable ${key}`)
	})
	if (!Array.isArray(event.items)) errors.push('event.items must be an array')
	if (event.items.length > event.options?.REPLAY_BATCH_SIZE) errors.push(`too many items (REPLAY_BATCH_SIZE = ${event.options.REPLAY_BATCH_SIZE})`)

	if (errors.length > 0) {
		throw new ValidationError('VALIDATION_ERROR', errors)
	} else {
		return event
	}
}

class ValidationError extends Error {
	constructor (message, errors) {
		super(message)
		this.type = 'ValidationError'
		if (Array.isArray(errors)) {
			this.stack = errors.join('\n')
			this.errors = errors
		}
	}
}
