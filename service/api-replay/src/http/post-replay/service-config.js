const SOLID_HOST = process.env.SOLID_HOST ? process.env.SOLID_HOST : false

if (!SOLID_HOST) {
	throw new Error(
		'Please set SOLID_HOST environment variable to the host name of the root pod i.e. bhhs.dev.yodata.io'
	)
}

/**
  * @typedef PostApiReplayRequest
  * @member {string} SOLID_HOST - hostname of the root pod i.e. bhhs.dev.yodata.io
  */
const ApiReplayConfig = {
	SOLID_HOST
}

module.exports = ApiReplayConfig
