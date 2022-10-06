const payload = require('./transactionreport')

module.exports = {
	valid: { object: payload },
	invalid: {
		topic: 'unknown'
	},
	getRequest: (overrides) => {
		const response = {
			object: Object.assign({}, payload, overrides)
		}
		return response
	}
}
