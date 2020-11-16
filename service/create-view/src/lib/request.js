const yaml = require('js-yaml')
const got = require('got')
const logger = require('../lib/logger')
const client = got.extend({
	hooks: {
		beforeRequest: [
			logRequest
		],
		afterResponse: [
			parseResponseData
		]
	}
})

module.exports = client

function logRequest(request) {
	logger.debug(`${request.method} ${request.href}`)
	return request
}

async function parseResponseData(response) {
	if (response && response.headers && response.headers[ 'content-type' ]) {
		const contentType = String(response.headers[ 'content-type' ]).toLowerCase()  
		if (contentType.includes('json')) {
			response.data = JSON.parse(response.body)
		} else if (contentType.includes('yaml')) {
			response.data = yaml.load(response.body)
		}
	}
	return response
}
