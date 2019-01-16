// @ts-check
const getHeader = require('./get-header-value')
const typeis = require('type-is')
/**
 * 
 * @param {object} httpMessage
 * @param {object} httpMessage.headers
 * @returns {boolean}
 */
const hasData = (httpMessage) => {
	const contentType = getHeader(httpMessage,'content-type')
	return typeis.is(contentType,['json','+json']) !== false
}

module.exports = hasData
