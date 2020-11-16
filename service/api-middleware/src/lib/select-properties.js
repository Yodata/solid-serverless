const pick = require('just-pick')
const get = require('get-value')
const set = require('set-value')
const has = require('./object-has')
const clone = require('just-clone')

/**
 *
 * @param {string} key - the key to search
 * @param {string} propList - comma separated list of keys to return from the input item
 */
function selectProperties(key, propList = '') {
	return (info) => {
		let next = clone(info)
		if (has(next, key)) {
			let properties = propList.split(',')
			let object = get(next, key, {})
			set(next, key, pick(object, properties))
		}
		return next
	}
}

module.exports = selectProperties