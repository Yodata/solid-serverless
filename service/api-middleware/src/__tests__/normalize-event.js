/* eslint-disable no-undef */

const normalizeEvent = require('../normalize-event')

const createRequest = (contentType,data) => {
	return {
		headers: {
			'content-type': contentType
		},
		body: new Buffer(JSON.stringify(data)).toString('base64'),
		isBase64Encoded: true
	}
}

test('request-event', () => {
	const eventData = {'foo':'bar'}
	const event = {
		request: createRequest('application/json', eventData)
	}
	const result = normalizeEvent(event)
	expect(result).toHaveProperty('stage', 'request')
	expect(result).toHaveProperty('hasData', true)
	expect(result).toHaveProperty('contentType', 'application/json')
	expect(result).toHaveProperty('object', eventData)
})

test('response-event', () => {
	const eventData = {'foo':'bar'}
	const event = {
		response: createRequest('application/json', eventData)
	}
	const result = normalizeEvent(event)
	expect(result).toHaveProperty('stage', 'response')
	expect(result).toHaveProperty('hasData', true)
	expect(result).toHaveProperty('contentType', 'application/json')
	expect(result).toHaveProperty('object', eventData)
})