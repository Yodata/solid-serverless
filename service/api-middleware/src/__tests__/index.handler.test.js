/* eslint-disable no-undef */
const handler = require('..').handler
const getData = require('../lib/get-event-data')

test('returns a Promise', async () => {
	expect(handler).toBeInstanceOf(Function)
	const event = require('../example/response')
	const response = handler(event, {})
	return expect(response).toBeInstanceOf(Promise)
})

test('example event/response', async () => {
	expect.assertions(1)
	const event = require('../example/event')
	const response = require('../example/response')
	return expect(handler(event, {})).resolves.toEqual(response)
})

test('parses uri object keys', async () => {
	expect.assertions(1)
	const event = require('../example/test-event')
	const data = getData(event)
	const response = await handler(event, {})
	return expect(response.object).toEqual(data)
})

test('get request with content-type header', async () => {
	const event = module.exports = {
		'request': {
			'method': 'GET',
			'headers': {
				'Content-Type': [
					'application/json'
				]
			},
			'body': ''
		},
		'scope': [],
		'policy': []
	}
	const result = await handler(event, {})
	return expect(result).toHaveProperty('hasData', false)
})

test('content-type with no object does not crash', async () => {
	const event = {
		'request': {
			'method': 'GET',
			'url': 'https://dave.dev.yodata.io/public/',
			'body': '',
			'isBase64Encoded': true
		},
		'scope': [],
		'policy': {}
	}
	return expect(handler(event, {})).resolves.toEqual(event)
})