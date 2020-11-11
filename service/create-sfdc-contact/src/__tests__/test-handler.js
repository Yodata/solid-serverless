// @ts-nocheck
const handler = require('..').handler

describe('Tests index', () => {
	test('verifies successful response', async () => {
		const event = require('../example/event.json')
		// const response = require('../example/response.json')
		const response = event
		const result = await handler(event, {})
		return expect(result).toMatchObject(response)
	})
})
