const handler = require('..').handler

describe('check-scope.handler', () => {
	test('example event/response', () => {
		const event = require('../example/event.json')
		const response = require('../example/response.json')
		return expect(handler(event)).resolves.toEqual(response)
	})
})