const query = require('queryl')

module.exports = function (filter, object) {
	if (!filter) return true
	return query.match(filter, object)
}
