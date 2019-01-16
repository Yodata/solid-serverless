/* eslint-disable no-undef */
const handler = require('..').handler
const getData = require('../lib/get-event-data')

describe('api-middleware', () => {
	
	test('returns a Promise', () => {
		expect(handler).toBeInstanceOf(Function)
		const event = require('../example/response.json')
		const response = handler(event)
		return expect(response).toBeInstanceOf(Promise)
	})

	test('example event/response', () => {
		const event = require('../example/event.json')
		const response = require('../example/response.json')
		expect(handler(event)).resolves.toEqual(response)
	})

	test('parses uri object keys', async () => {
		const event = require('../example/test-event.json')
		const data = getData(event)
		const response = await handler(event)
		return expect(response.object).toEqual(data)
	})
})

