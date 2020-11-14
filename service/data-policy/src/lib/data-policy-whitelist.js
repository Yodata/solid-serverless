const logger = require('./logger')
const { URL } = require('url')


/**
 * checks returns true if event.agent is on the white list
 * @param {object} event
 * @param {string} [event.agent]
 * @returns {boolean}
 */
function isWhiteListed(event) {
	const { DATA_POLICY_SVC_HOST, DATA_POLICY_WL, SOLID_HOST = 'bhhs.dev.yodata.io' } = process.env

	// public request (no-agent) never white listed
	if (!String(event.agent).startsWith('http')) {
		return false
	}

	const agent_host = new URL(event.agent).hostname

	// master account always whitelisted
	if (agent_host === SOLID_HOST) {
		logger.debug('AGENT_IS_SOLID_HOST')
		return true
	}

	// data policy svc always whitelisted
	if (agent_host === DATA_POLICY_SVC_HOST) {
		logger.debug('AGENT_IS_DATA_POLICY_SVC_HOST')
		return true
	}

	// check env var DATA_POLICY_WL comma separated list of hostnames or hostname fragments
	if (String(DATA_POLICY_WL).length > 0) {
		const whitelist = String(DATA_POLICY_WL).split(',')
		const index = whitelist.findIndex((v) => (String(agent_host).includes(v)))
		return (index >= 0) ? true : false
	}
	return false
}

module.exports = isWhiteListed