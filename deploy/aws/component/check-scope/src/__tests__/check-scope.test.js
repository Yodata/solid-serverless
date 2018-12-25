const handler = require('..').handler
const Event = require('../../event.json')
const Response = require('../../response.json')
let event, context

describe('Tests index', function () {

    beforeEach(() => {
        event = Object.assign({},Event)
    })
    
    test('verifies successful response', async () => {
        const result = await handler(event, context)
        return expect(result).toHaveProperty('isAllowed')
    });

    test('empty scope is always allowed', async () => {
        event.scope = {}
        const result = await handler(event, context)
        return expect(result).toHaveProperty('isAllowed', true)
    })

    test('missing .scope property is an error and not allowed', async () => {
        delete event.scope
        const result = await handler(event)
        return expect(result).toHaveProperty('isAllowed', false)
    })
});

