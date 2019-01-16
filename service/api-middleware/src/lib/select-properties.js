const pick = require('lodash/pick')
const get = require('lodash/get')
const set = require('lodash/set')
const has = require('lodash/has')
const clone = require('lodash/cloneDeep')

const selectProperties = (key, propList='') => (info) => {
	let next = clone(info)
	if (has(next,key)) {
		let properties = propList.split(',')
		let object = get(next,key,{})
		set(next,key,pick(object,properties))
	}
	return next
}

module.exports = selectProperties