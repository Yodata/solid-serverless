const checkScope = require('../check-scope')

describe('check-scope', () => {
    
    test('exports handler function', () => {
        expect(checkScope).toHaveProperty('handler')
        expect(checkScope.handler).toBeInstanceOf(Function)
    })
})