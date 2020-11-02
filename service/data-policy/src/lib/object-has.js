const getvalue = require('get-value')

/**
 * test the existence of value of an object item
 * @param {object} object - the item to test
 * @param {string} key - the key
 * @param {*} [comparator] - test the value against comparator
 *
 * @returns {boolean}
 */
module.exports = function has (object, key, comparator) {
	const value = getvalue(object, key)
	switch (typeof comparator) {
	case 'undefined':
		return typeof value !== 'undefined'
	case 'function':
		return comparator(value)
	default:
		return value == comparator
	}
}
