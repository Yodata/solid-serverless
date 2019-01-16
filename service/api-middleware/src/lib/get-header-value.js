// @ts-check
const getHeaders = require('./get-headers')

/**
 * retrieves a header key (normalizes header & keyname)
 * @param {object} httpMessage - and HTTP.Request object
 * @param {string} keyname
 * @param {*} [defaultValue]
 */
module.exports = (httpMessage, keyname, defaultValue) => {
	const headers = getHeaders(httpMessage)
	const key = keyname.toLowerCase()
	return headers[key] || defaultValue
}