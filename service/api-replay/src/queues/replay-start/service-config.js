
const SOLID_STORE = process.env.SOLID_STORE ? process.env.SOLID_STORE : false
const SOLID_HOST = process.env.SOLID_HOST ? process.env.SOLID_HOST : false
const AWS_REGION = process.env.AWS_REGION || 'us-west-2'
const REPLAY_BATCH_SIZE = process.env.REPLAY_BATCH_SIZE || 100
const REPLAY_ITEM_LIMIT = process.env.REPLAY_ITEM_LIMIT || 10000

if (!SOLID_STORE) {
	throw new Error(
		'Please set SOLID_STORE environment variable to the name of solid-storage bucket'
	)
}

if (!SOLID_HOST) {
	throw new Error(
		'Please set SOLID_HOST environment variable to the host name of the root pod i.e. bhhs.dev.yodata.io'
	)
}

/**
  * @typedef DEFAULT_CONFIG
  * @member {string} SOLID_HOST - hostname of the root pod i.e. bhhs.dev.yodata.io
  * @member {string} SOLID_STORE - bucket name of the solid-serverless-storage bucket
	* @member {string} AWS_REGION - region of the root solid-serverless bucket @example us-west-2
 * 	@member {string} REPLAY_BATCH_SIZE - number of items to replay in a single batch
 *  @member {string} REPLAY_ITEM_LIMIT - limit replay requests to this number of items
  */
const DEFAULT_CONFIG = {
	SOLID_STORE,
	SOLID_HOST,
	AWS_REGION,
	REPLAY_BATCH_SIZE: Number(REPLAY_BATCH_SIZE),
	REPLAY_ITEM_LIMIT: Number(REPLAY_ITEM_LIMIT)
}

module.exports = DEFAULT_CONFIG
