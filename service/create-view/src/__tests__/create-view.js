const createView = require('../lib/create-view')
const event = require('../example/event.json')
const response = require('../example/response.json')

const context = {}

describe('create-view', () => {
	test('example event/response', () => {
		const result = createView(event)
		return expect(result).resolves.toEqual(response)
	})
})

