
const SOLID_HOST = process.env.SOLID_HOST ? process.env.SOLID_HOST : false
const SVC_KEY = process.env.SVC_KEY ? process.env.SVC_KEY : false
const REPLAY_BATCH_SIZE = process.env.REPLAY_BATCH_SIZE ? process.env.REPLAY_BATCH_SIZE : 100
const REPLAY_FILTERING_ENABLED = process.env.REPLAY_FILTERING_ENABLED ? process.env.REPLAY_FILTERING_ENABLED : false
const REPLAY_ITEM_CONCURRENCY = process.env.REPLAY_ITEM_CONCURRENCY ? Number(process.env.REPLAY_ITEM_CONCURRENCY) : 1
const STOP_REPLAY_ON_ERROR = process.env.STOP_REPLAY_ON_ERROR ? process.env.STOP_REPLAY_ON_ERROR : true

if (!SVC_KEY) {
	throw new Error(
		'Please set SVC_KEY environment variable'
	)
}

if (!SOLID_HOST) {
	throw new Error(
		'Please set SOLID_HOST environment variable to the host name of the root pod i.e. bhhs.dev.yodata.io'
	)
}

/**
  * @typedef ReplayItemsConfig
  * @member {string} SOLID_HOST - hostname of the root pod i.e. bhhs.dev.yodata.io
	* @member {string} SVC_KEY - credentials (service requires solid-admin permissions)
  */
const ReplayItemsCongig = {
	SOLID_HOST,
	SVC_KEY,
	REPLAY_BATCH_SIZE,
	REPLAY_FILTERING_ENABLED,
	REPLAY_ITEM_CONCURRENCY,
	STOP_REPLAY_ON_ERROR
}

module.exports = ReplayItemsCongig
