const handler = require('..').handler
const event = require('../example/event.json')
const response = require('../example/response.json')
const context = {}

describe('Tests index', function () {
    test('verifies successful response', async () => {
        const result = await handler(event, context)
        return expect(result).toMatchObject(response)
    })
})

