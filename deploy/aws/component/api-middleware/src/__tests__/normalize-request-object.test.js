const normalizeRequestObject = require('../normalize-request-object')

test('normalizeRequestObject', async () => {
    let requestObject = {type:'request'}
    let event = {
        request: {
            method: "POST",
            rawHeaders: {
                "content-type": ['application/json']
            },
            body: JSON.stringify(requestObject)
        }
    }
    event = await normalizeRequestObject(event)
    expect(event.object).toEqual(requestObject)
})

test('response-context', async () => {
    let responseObject = {type: 'response'}
    let event = {
        response: {
            headers: {
                'Content-Type': ['application/json']
            },
            body: JSON.stringify(responseObject)
        }
    }
    event = await normalizeRequestObject(event)
    expect(event.object).toEqual(responseObject)
})
