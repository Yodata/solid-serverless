const { createEvent, createRequest } = require('../../example')
const redactPassWordPolicy  = require('./redact-password-policy.js')

module.exports = {
	createEvent,
	createRequest,
	policy: {
		global: {
			redactPassWordPolicy
		}
	}
}