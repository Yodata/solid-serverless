describe('@yodata/solid-tools/client', () => {
    const client = require('../lib/client')
    test('constructor', () => {
        expect(client).toBeInstanceOf(Function)
        expect(client('testuser')).toBeInstanceOf(Function)
    })
})