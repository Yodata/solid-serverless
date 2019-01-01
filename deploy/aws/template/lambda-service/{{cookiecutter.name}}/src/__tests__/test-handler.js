const handler = require('..').handler

describe('Tests index', function () {

    test('verifies successful response', async () => {
        const event = require('../example/event.json')
        const response = require('../example/response.json')
        const result = await handler(event)
        return expect(result).toMatchObject(response)
    });
});

