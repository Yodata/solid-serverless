const LambdaTester = require('lambda-tester')
const createView = require('../create-view')

describe('check-scope', () => {    
    
    test('exports handler function', () => {
        expect(createView).toHaveProperty('handler')
        expect(createView.handler).toBeInstanceOf(Function)
    })
    
    test('handler', () => {
        const handler = createView.handler
        const event = {
            object: {type: 'cat'},
            scope: {},
            policy: {}
        }
        return expect(handler(event)).resolves.toMatchObject(event)
    })
})