const handler = require('..').handler

describe('echo-service.handler', function () {
    
    test('.response', async () => {
        const event = require('../example/event.json')
        const response = require('../example/response.json')
        const result = await handler(event)
        return expect(result).toMatchObject(response)
    });
});

