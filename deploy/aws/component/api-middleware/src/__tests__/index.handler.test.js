/* eslint-disable no-undef */
const handler = require('..').handler

describe('api-middleware', () => {
	
	test('returns a Promise', () => {
		expect(handler).toBeInstanceOf(Function)
		const event = require('../example/response.json')
		const response = handler(event)
		return expect(response).toBeInstanceOf(Promise)
	})

	test('example event/response', async () => {
		const event = require('../example/event.json')
		const expectedResponse = require('../example/response.json')
		const response = await handler(event)
		return expect(response).toEqual(expectedResponse)
	})
})

