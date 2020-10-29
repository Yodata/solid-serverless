const handler = require('..').handler

describe('echo-service.handler', () => {
	test('.response', async () => {
		const event = require('../example/event.json')
		const response = require('../example/response.json')
		// @ts-ignore
		const result = await handler(event)
		return expect(result).toMatchObject(response)
	})
})
