
const SOLID_HOST = process.env.SOLID_HOST ? process.env.SOLID_HOST : false
const SVC_KEY = process.env.SVC_KEY ? process.env.SVC_KEY : false

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
  * @typedef ReplayItemsCongig
  * @member {string} SOLID_HOST - hostname of the root pod i.e. bhhs.dev.yodata.io
	* @member {string} SVC_KEY - credentials (service requires solid-admin permissions)
  */
const ReplayItemsCongig = {
	SOLID_HOST,
	SVC_KEY
}

module.exports = ReplayItemsCongig
