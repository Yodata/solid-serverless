const handler = require('..').handler
const event = require('../../event.json')
const response = require('../../response.json')

const context = {}

describe('create-view', () => {
	test('example event/response', async () => {
		const result = await handler(event, context)
		return expect(result).toMatchObject(response)
	})
})

