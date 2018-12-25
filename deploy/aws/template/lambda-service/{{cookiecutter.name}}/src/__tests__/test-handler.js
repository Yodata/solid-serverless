const handler = require('..').handler
const event = require('../../event.json')
const response = require('../../response.json')
const context = {}

describe('Tests index', function () {
    test('verifies successful response', async () => {
        const result = await handler(event, context)
        return expect(result).toMatchObject(response)
    });
});

