const handler = require('..').handler
describe('check-scope.handler', () => {
	test('example event/response', () => {
		const event = require('../example/event.json')
		const response = require('../example/response.json')
		return expect(handler(event)).resolves.toEqual(response)
	})

	test('handler errors', async () => {
		expect.assertions(1)
		const object = {type:'test'}
		const scope = {
			a: 'http://error'
		}
		return expect(handler({object,scope})).resolves.toHaveProperty('isAllowed', false)
	})
})