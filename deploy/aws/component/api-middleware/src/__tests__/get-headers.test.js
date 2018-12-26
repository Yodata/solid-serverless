const getHeaders = require('../get-headers')

test('getHeaders', () => {
    let message = {
        rawHeaders: {
            'Content-Type': ['application/json']
        }
    }
    let headers = getHeaders(message)
    expect(headers).toHaveProperty('content-type', 'application/json')
})