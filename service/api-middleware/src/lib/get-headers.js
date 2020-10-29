const transform = require('lodash.transform')

/**
 * Transform httpMessage.rawHeaders -> headers
 * @param {object} httpMessage
 * @param {object} [httpMessage.rawHeaders]
 * @param {object} [httpMessage.headers]
 * @returns {object}
 */
module.exports = httpMessage => {
	const headers = httpMessage.headers || httpMessage.rawHeaders || {}
	return transform(headers, (object, value, key) => {
		const K = String(key).toLowerCase()
		let V
		if (Array.isArray(value)) {
			if (value.length === 1) {
				V = value[0]
			} else {
				V = value.join(',')
			}
		} else {
			V = value
		}
		object[K] = V
		return object
	})
}
