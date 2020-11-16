// @ts-check
const getHeader = require('./get-header-value')
const typeis = require('type-is')
/**
 * 
 * @param {object} httpMessage
 * @param {object} httpMessage.headers
 * @param {string} [httpMessage.body]
 * @returns {boolean}
 */
function hasData(httpMessage) {
	const contentType = getHeader(httpMessage, 'content-type')
	const { body } = httpMessage
	return (
		typeis.is(contentType, ['json', '+json', 'application/x-yaml']) !== false && typeof body === 'string' && body.length > 0
	)
}

module.exports = hasData
