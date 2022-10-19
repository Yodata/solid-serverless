const { REPLAY_BATCH_SIZE } = require('./service-config')

module.exports = async function _validateInput (event) {
	if (!Array.isArray(event.items)) { throw new ValidationError('event.items must be an array') }
	if (event.items.length > REPLAY_BATCH_SIZE) { throw new ValidationError(`EVENT_ITEMS_EXCEEDS_REPLAY_BATCH_SIZE:${event.items.length}`) }
	return event
}

class ValidationError extends Error {
	constructor (message, props) {
		super(message)
		this.type = 'ValidationError'
		Object.assign(this, props)
	}
}
