const createContext = require('./create-context')

test('request-context', async () => {
    let event = {
        request: {
            method: "POST",
            rawHeaders: {
                "content-type": ['application/json']
            },
            body: JSON.stringify({type: 'test'})
        }
    }
    event = await createContext(event)
    expect(event).toHaveProperty('requestMethod', 'POST')
    expect(event).toHaveProperty('contentType', 'application/json')
    expect(event.object).toEqual({type:'test'})
})

test('response-context', async () => {
    let event = {
        request: {
            method: "GET",
            rawHeaders: {
                "content-type": ['application/json']
            },
            body: JSON.stringify({type: 'test'})
        },
        response: {
            method: "POST",
            headers: {
                'Content-Type': ['application/ld+json']
            },
            body: JSON.stringify({type: 'response'})
        }
    }
    event = await createContext(event)
    expect(event).toHaveProperty('requestMethod', 'GET')
    expect(event).toHaveProperty('contentType', 'application/ld+json')
    expect(event.object).toEqual({type:'response'})
})
