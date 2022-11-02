const defaultOptions = {
	SOLID_HOST: 'http://example.com',
	SVC_KEY: 'xxx',
	REPLAY_BATCH_SIZE: 100,
	REPLAY_FILTERING_ENABLED: true,
	REPLAY_ITEM_CONCURRENCY: 1,
	STOP_REPLAY_ON_ERROR: true
}
const getOptions = (overrides) => Object.assign({}, defaultOptions, overrides)

const defaultEvent = {
	target: 'http://example.com/inbox/',
	items: []
}
const getEvent = (overrides) => Object.assign({}, defaultEvent, { options: getOptions() }, overrides)

module.exports = {
	getOptions,
	getEvent
}
