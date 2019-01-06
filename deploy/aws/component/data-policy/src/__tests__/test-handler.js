describe('Tests index', () => {
	const handler = require('..').handler
	const event = require('../example/event.json')
	const response = event
	const context = {}
	test('verifies successful response', async () => {
		const result = await handler(event, context)
		return expect(result).toMatchObject(response)
	})
})

