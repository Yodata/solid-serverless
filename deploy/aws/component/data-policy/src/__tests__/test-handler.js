const {describe,test,expect} = require('jest')

describe('Tests index', function () {
    const handler = require('..').handler
    const event = require('../../event.json')
    const response = require('../../response.json')
    const context = {}
    test('verifies successful response', async () => {
        const result = await handler(event, context)
        return expect(result).toMatchObject(response)
    })
})

